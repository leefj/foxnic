package com.github.foxnic.dao.queue;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.api.queue.QueueStatus;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.collection.MapUtil;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 基于数据库乐观锁的简单消息队列
 */
public abstract class DecouplingMessageQueue<M extends Entity> {

    protected DAO dao;
    private String table;
    private int ignoreSeconds;
    private String idField;
    private String statusField;
    private String pushTimeField;
    private String retryField;
    private String statusTimeField;
    private String errorField;
    private String resultField;
    private String queueIdField;
    //
    private int statusExpireSeconds;

//    /**
//     * 放弃时间，默认两小时
//     * */
//    private int abandonSeconds=60 * 60 * 8;
    //
    private SimpleTaskManager simpleTaskManager;
    private Class<M> messagePoType=null;
    private int consumerCount=2;
    private int threadCount=consumerCount+1;
    private long consumerCheckInterval=1000L;
    private int fetchSize=64;

    private int maxQueueSize=256;

//    /**
//     * 是否按顺序消费
//     * */
//    private boolean keepOrder=false;
//
//    /**
//     * 按顺序消费时的隔离字段
//     * */
//    private String[] keepOrderFields=null;

//    /**
//     * 保持消费的顺序
//     * */
//    public void keepOrder(String... fields) {
//        this.keepOrder=true;
//        this.keepOrderFields=fields;
//    }

    /**
     * 重试次数
     * */
    private int maxRetrys=0;

    private Set<String> idsInQueue=Collections.synchronizedSet(new HashSet<>());
    private LinkedBlockingQueue<Message<M>> queue = new LinkedBlockingQueue<>();
    private Long queueFetchTime;
    private List<Consumer> consumers;

    private DBColumnMeta deletedField=null;

    private String queueId;

    public DecouplingMessageQueue(DAO dao, String table, String idField, String statusField, String pushTimeField,String statusTimeField,String retryField,String errorField,String resultField,String queueIdField,int ignoreSeconds,int statusExpireSeconds) {
        this.dao = dao;
        this.table = table;
        this.idField = idField;
        this.statusField = statusField;
        this.pushTimeField = pushTimeField;
        this.statusTimeField = statusTimeField;
        this.ignoreSeconds=ignoreSeconds;
        this.statusExpireSeconds=statusExpireSeconds;
        this.retryField=retryField;
        this.errorField=errorField;
        this.resultField=resultField;
        this.queueIdField=queueIdField;

        this.queueId= IDGenerator.getNanoId(18);

        ParameterizedType type=(ParameterizedType)this.getClass().getGenericSuperclass();
        Type[] types=type.getActualTypeArguments();
        messagePoType=(Class)types[0];

        //TODO 增加字段校验逻辑
        validate();


    }

    private void validate() {
        if(dao==null) return;
        this.deletedField=dao.getTableColumnMeta(table,dao.getDBTreaty().getDeletedField());
    }



    /**
     * 将消息 push 到队列
     * */
    public void push(M... message) {
        List<M> list= Arrays.asList(message);
        this.push(list);
    }

    /**
     * 将消息 push 到队列
     * */
    public void push(List<M> messageList) {
        Date nowDate=new Date();
        Timestamp nowTime=new Timestamp(System.currentTimeMillis());
        // 设置控制字段
        for (M m : messageList) {
            BeanUtil.setFieldValue(m,statusField, QueueStatus.waiting.code());
            BeanUtil.setFieldValue(m,pushTimeField,nowDate);
            BeanUtil.setFieldValue(m,statusTimeField,nowTime);
            BeanUtil.setFieldValue(m,retryField,0);
        }
        dao.pausePrintThreadSQL();
        dao.insertEntities(messageList);
        dao.resumePrintThreadSQL();


        // 有新消息时理解进入一次消费
        for (Consumer consumer : consumers) {
            simpleTaskManager.doDelayTask(consumer,0);
        }

    }



    /**
     * 启动
     * */
    public void start() {
        if(simpleTaskManager!=null) {
            return;
        }
        this.fetchIntoQueue();
        simpleTaskManager=new SimpleTaskManager(threadCount,this.getClass().getSimpleName());
        consumers=new ArrayList<>();
        for (int i = 0; i < consumerCount; i++) {
            Consumer consumer=new Consumer(this);
            consumers.add(consumer);
            simpleTaskManager.doIntervalTask(consumer,consumerCheckInterval);
            try {
                Thread.sleep(200L);
            } catch (Exception e){}
        }

        // 消息续租心跳
        simpleTaskManager.doIntervalTask(new Runnable() {
            @Override
            public void run() {
                beat();
            }
        },statusExpireSeconds*1000/3);

    }

    /**
     * 提取消息ID
     * */
    private String getMessageId(M message) {
        return BeanUtil.getFieldValue(message,idField,String.class);
    }

    /**
     * 刷新/重新从数据库读取消息
     * */
    private M refresh(M message) {
        Select select=new Select(this.table);
        select.select("*");
        select.where().and(idField+"=?",this.getMessageId(message));
        return dao.queryEntity(this.messagePoType,select);
    }

    /**
     * 更新消息状态，返回成功或失败
     * */
    void updateInfo (M message, Exception exception,Result result,Integer retrys) {

        Update update=new Update(this.table);
        // 组装主键条件
        update.where().and(idField+"=?",this.getMessageId(message));

        // 更新数据库
        if(exception!=null) {
            update.set(errorField,"error when consuming:\n"+StringUtil.toString(exception));
        } else {
            update.set(errorField,null);
        }
        if(result!=null) {
            if(exception==null && result.failure() && result.extra().getException()!=null) {
                update.set(errorField,"error in result:\n"+result.extra().getException());
            }
            update.set(resultField, JSON.toJSONString(result));
        } else {
            update.set(resultField, null);
        }
        if(retrys!=null) {
            update.set(retryField, retrys);
        }
        dao.execute(update);
    }


    private boolean updateStatus (M message, QueueStatus status) {
        return updateStatus(message,status,null);
    }
    /**
     * 更新消息状态，返回成功或失败
     * */
    private boolean updateStatus (M message, QueueStatus status,Timestamp statusTime) {
        if(statusTime==null) {
            statusTime = new Timestamp(System.currentTimeMillis());
        }
        Update update=new Update(this.table);
        // 组装主键条件
        update.where().and(idField+"=?",getMessageId(message));

        // 组装原始状态条件
        QueueStatus originalStatus=BeanUtil.getFieldValue(message,statusField,QueueStatus.class);
        update.where().and(statusField+"=?",originalStatus.code());

        // 更新数据库
        update.set(statusField,status.code()).set(statusTimeField,statusTime).set(queueIdField,queueId);
        int z=dao.execute(update);
        if(z>0) {
            // 设置新的状态和新的状态变更时间
            BeanUtil.setFieldValue(message,statusField,status.code());
            BeanUtil.setFieldValue(message,statusTimeField,statusTime);
            return true;
        } else {
            return false;
        }
    }

    private long beatTime=0;

    void beat() {

        if(idsInQueue.isEmpty()) return;

        if(System.currentTimeMillis() - beatTime< (statusExpireSeconds *1000)/2 ) {
            return;
        }

        beatTime=System.currentTimeMillis();

        Update update=new Update(table);
        update.set(statusTimeField,new Timestamp(beatTime));

        In in=new In(idField,idsInQueue);
        update.where().and(in);

        dao.pausePrintThreadSQL();
        dao.execute(update);
        dao.resumePrintThreadSQL();

    }

    /**
     * 是否是一个最早的消息
     * */
    public boolean isEarliest(Message<M> message) {
//        if(!keepOrder) {
//            return true;
//        }
        return true;
    }

    /**
     * 将待处理的数据去入内存队列
     * */
    synchronized int fetchIntoQueue() {
        // 未到取数时间不需要取数
        if(queueFetchTime!=null && System.currentTimeMillis()-queueFetchTime < consumerCheckInterval) {
            return queue.size();
        }
        queueFetchTime=System.currentTimeMillis();

        String deletedSubSQL="";
        if(this.deletedField!=null) {
            deletedSubSQL=" and "+this.deletedField.getColumn()+" = 0";
        }

        String retrySubSQL="";
        if(maxRetrys>0) {
            retrySubSQL=" and "+this.retryField+" < "+maxRetrys;
        }

        Date pushExpireTime=new Date();
        pushExpireTime= DateUtil.addSeconds(pushExpireTime,-ignoreSeconds);

        Date statusExpireTime=new Date();
        statusExpireTime= DateUtil.addSeconds(statusExpireTime,-statusExpireSeconds);


        Map<String,Object> params= MapUtil.asMap(
                "pushExpireTime",pushExpireTime,
                "statusExpireTime",statusExpireTime
        );


        String[] querySQL = {
                "select * from (",
                // waiting : 加入时间有效的情况下，都处理。
                "select * from " + table + " where " + statusField + " = '" + QueueStatus.waiting.code() + "' " + deletedSubSQL + (ignoreSeconds > 0 ? (" and " + pushTimeField + " > :pushExpireTime") : ""),
                "union",
                // queue(排队中)： 在重试次数范围未内的允许不断重试；如果创建时间已经很久了，则不再尝试；如果刚刚重试过也不再重试
                "select * from " + table + " where " + statusField + " = '" + QueueStatus.queue.code() + "' " + deletedSubSQL + retrySubSQL + (ignoreSeconds > 0 ? (" and " + pushTimeField + " > :pushExpireTime") : "") + (statusExpireSeconds > 0 ? (" and " + statusTimeField + " < :statusExpireTime") : ""),
                "union",
                // consuming(处理中)： 在重试次数范围未内的允许不断重试；如果创建时间已经很久了，则不再尝试；如果刚刚重试过也不再重试
                "select * from " + table + " where " + statusField + " = '" + QueueStatus.consuming.code() + "' " + deletedSubSQL + retrySubSQL + (ignoreSeconds > 0 ? (" and " + pushTimeField + " > :pushExpireTime") : "") + (statusExpireSeconds > 0 ? (" and " + statusTimeField + " < :statusExpireTime") : ""),
                "union",
                // 处理失败的：在重试次数范围未内的允许不断重试；如果创建时间已经很久了，则不再尝试；如果刚刚重试过也不再重试
                "select * from " + table + " where " + statusField + " = '" + QueueStatus.failure.code() + "' " + deletedSubSQL + retrySubSQL + (ignoreSeconds > 0 ? (" and " + pushTimeField + " > :pushExpireTime") : "") + (statusExpireSeconds > 0 ? (" and " + statusTimeField + " < :statusExpireTime") : ""),
                ") t order by " + pushTimeField + " asc"
        };

        Expr select=new Expr(SQL.joinSQLs(querySQL),params);

        dao.pausePrintThreadSQL();
        // 查询
        PagedList<M> messageList=dao.queryPagedEntities(messagePoType,fetchSize,1,select);
        String id=null;
        for (M m : messageList) {
            // 如果锁定成功，则加入到队列
            QueueStatus originalStatus=BeanUtil.getFieldValue(m,statusField,QueueStatus.class);
            Timestamp originalStatusTime=BeanUtil.getFieldValue(m,statusTimeField,Timestamp.class);
            if(updateStatus(m,QueueStatus.queue)) {
                boolean suc=false;
                try {
                    id = getMessageId(m);
                    if (!idsInQueue.contains(id)) {
                        suc = queue.add(new Message<>(m, id));
                        if(suc) {
                            idsInQueue.add(id);
                        }
                    }

                    if(queue.size()>maxQueueSize) {
                        break;
                    }

                } catch (Exception e) {
                    suc = false;
                }
                // 如果加入队列失败
                if(!suc) {
                    // 如果前一步锁定是成功的，那么这一步理论上是一定会成功的
                    suc = updateStatus(m,originalStatus,originalStatusTime);
                    if(!suc) {
                        RuntimeException ex=new RuntimeException("消息在恢复非锁定状态时失败");
                        Logger.exception(ex);
                        throw ex;
                    }
                }
            }
        }
        dao.resumePrintThreadSQL();
        return queue.size();

    }

    Message<M> poll() {
//        while (true) {
            Message<M> message = queue.poll();
//            if (isEarliest(message)) {
                return message;
//            } else {
//                // 如果不是最早的消息，就继续放回队列排队
//                if(message.increaseAddTimes()<4) {
//                    queue.add(message);
//                }
//            }
//        }
    }

    /**
     * 消费消息
     * */
    void consumeInternal(Message<M> message) {
        M messageBody=message.getMessage();

        boolean suc=updateStatus(messageBody,QueueStatus.consuming);
        if(!suc) {
            idsInQueue.remove(message.getId());
            return;
        }

        Integer retrys=BeanUtil.getFieldValue(messageBody,retryField,Integer.class);

        try {
            retrys++;
            Result result=this.consume(messageBody);
            suc=updateStatus(messageBody,QueueStatus.consumed);
            if(suc) {
                idsInQueue.remove(message.getId());
                updateInfo(messageBody,null,result,retrys);
            } else {
                idsInQueue.remove(message.getId());
                throw new RuntimeException("异常-01");
            }

        } catch (Exception e) {
            idsInQueue.remove(message.getId());
            suc=updateStatus(messageBody,QueueStatus.failure);
            // 理论上不会失败，暂不处理
            if(!suc) {
                throw new RuntimeException("异常-02");
            } else {
                updateInfo(messageBody,e,null,retrys);
            }
        }
    }

    protected abstract Result consume(M message) throws Exception;

    public void setDao(DAO dao) {
        this.dao = dao;
        this.validate();
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}

class Consumer<M extends Entity> implements Runnable {

    private DecouplingMessageQueue<M> messageQueue;

    public Consumer(DecouplingMessageQueue<M> messageQueue){
        this.messageQueue=messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            Message message=messageQueue.poll();

            if(message==null) {
                break;
            }
            messageQueue.consumeInternal(message);
        }
        //
        int left=messageQueue.fetchIntoQueue();
        if(left>0) {
            this.run();
        }

    }


}

class Message<M extends Entity> {
    private String id;
    private M message;
    private int retrys=0;

    private int addTimes=0;

    public int increaseAddTimes() {
        this.addTimes++;
        return addTimes;
    }

    public Message(M message, String id) {
        this.message=message;
        this.id=id;

    }

    public int getRetrys() {
        return retrys;
    }

    public void setRetrys(int retrys) {
        this.retrys = retrys;
    }

    public M getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }



}

