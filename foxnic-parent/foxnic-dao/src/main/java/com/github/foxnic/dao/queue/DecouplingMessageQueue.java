package com.github.foxnic.dao.queue;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.api.queue.QueueStatus;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanUtil;
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
 * 基于数据库的简单消息队列
 */
public abstract class DecouplingMessageQueue<M extends Entity> {

    protected DAO dao;
    private String table;
    private int ignoreSeconds;
    private String[] idFields;
    private String statusField;
    private String pushTimeField;
    private String retryField;
    private String statusTimeField;
    private String errorField;
    private String resultField;
    //
    private int statusExpireSeconds;
    //
    private SimpleTaskManager simpleTaskManager;
    private Class<M> messagePoType=null;


    private int consumerCount=2;

    private int threadCount=consumerCount+1;
    private long consumerCheckInterval=1000L;
    private int fetchSize=16;

    /**
     * 重试次数
     * */
    private int maxRetrys=0;

    private Set<String> idsInQueue=new HashSet<>();
    private LinkedBlockingQueue<Message<M>> queue = new LinkedBlockingQueue<>();
    private Long queueFetchTime;
    private List<Consumer> consumers;

    private DBColumnMeta deletedField=null;

    public DecouplingMessageQueue(DAO dao, String table, String[] idFields, String statusField, String pushTimeField,String statusTimeField,String retryField,String errorField,String resultField,int ignoreSeconds,int statusExpireSeconds) {
        this.dao = dao;
        this.table = table;
        this.idFields = idFields;
        this.statusField = statusField;
        this.pushTimeField = pushTimeField;
        this.statusTimeField = statusTimeField;
        this.ignoreSeconds=ignoreSeconds;
        this.statusExpireSeconds=statusExpireSeconds;
        this.retryField=retryField;
        this.errorField=errorField;
        this.resultField=resultField;

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


    public DecouplingMessageQueue(DAO dao, String table, String idField, String statusField, String pushTimeField,String statusTimeField,String retryField,String errorField,String resultField,int ignoreSeconds,int statusExpireSeconds) {
        this(dao, table, new String[]{idField}, statusField, pushTimeField,statusTimeField,retryField,errorField,resultField,ignoreSeconds,statusExpireSeconds);
    }

    public void push(M... message) {
        List<M> list= Arrays.asList(message);
        this.push(list);
    }

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
        }
    }

    private String getId(M message) {
        String[] ids=new String[idFields.length];
        Object idValue = null;
        int i=0;
        for (String idField : idFields) {
            idValue=BeanUtil.getFieldValue(message,idField);
            ids[i]=idValue.toString();
        }
        return StringUtil.join(ids,"-");
    }

    private M refresh(M message) {
        Select select=new Select(this.table);
        select.select("*");
        // 组装主键条件
        Object idValue = null;
        for (String idField : idFields) {
            idValue=BeanUtil.getFieldValue(message,idField);
            select.where().and(idField+"=?",idValue);
        }
        return dao.queryEntity(this.messagePoType,select);
    }

    /**
     * 更新消息状态
     * */
    boolean updateStatus (M message, QueueStatus status, Timestamp statusTime,Exception exception,Result result,Integer retrys) {

        Update update=new Update(this.table);
        // 组装主键条件
        Object idValue = null;
        for (String idField : idFields) {
            idValue=BeanUtil.getFieldValue(message,idField);
            update.where().and(idField+"=?",idValue);
        }
        // 组装原始状态条件
        QueueStatus originalStatus=BeanUtil.getFieldValue(message,statusField,QueueStatus.class);
        update.where().and(statusField+"=?",originalStatus.code());

        // 更新数据库
        update.set(statusField,status.code()).set(statusTimeField,statusTime);
        if(exception!=null) {
            update.set(errorField,StringUtil.toString(exception));
        } else {
            update.set(errorField,null);
        }
        if(result!=null) {
            update.set(resultField, JSON.toJSONString(result));
        } else {
            update.set(resultField, null);
        }
        if(retrys!=null) {
            update.set(retryField, retrys);
        }

        int z=dao.execute(update);
        if(z>0) {
            // 设置新的状态和新的状态变更时间
            BeanUtil.setFieldValue(message,statusField,status.code());
            BeanUtil.setFieldValue(message,statusTimeField,statusTime);
            if(retrys!=null) {
                BeanUtil.setFieldValue(message,retryField,retrys);
            }
            return true;
        } else {
            return false;
        }
    }



    /**
     * 将待处理的数据去入内存队列
     * */
    synchronized void fetchIntoQueue() {
        // 未到取数时间不需要取数
        if(queueFetchTime!=null && System.currentTimeMillis()-queueFetchTime < consumerCheckInterval) {
            return;
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


        String[] querySQL={
                // waiting : 加入时间有效的情况下，都处理。
                "select * from "+table+" where "+statusField+" = '"+QueueStatus.waiting.code()+"' "+deletedSubSQL +(ignoreSeconds>0 ? (" and "+pushTimeField+" > :pushExpireTime"):""),
                "union",
                // queue(排队中)+consuming(处理中)： 在重试次数范围未内的允许不断重试；如果创建时间已经很久了，则不再尝试；如果刚刚重试过也不再重试
                "select * from "+table+" where "+statusField+" in ('"+QueueStatus.queue.code()+"','"+QueueStatus.consuming.code()+"') "+deletedSubSQL+ retrySubSQL+(ignoreSeconds>0 ? (" and "+pushTimeField+" > :pushExpireTime"):"")+(statusExpireSeconds>0?(" and "+statusTimeField+" < :statusExpireTime"):""),
                "union",
                // 处理失败的：在重试次数范围未内的允许不断重试；如果创建时间已经很久了，则不再尝试；如果刚刚重试过也不再重试
                "select * from "+table+" where "+statusField+" = '"+QueueStatus.failure.code()+"' "+deletedSubSQL+retrySubSQL+(ignoreSeconds>0 ? (" and "+pushTimeField+" > :pushExpireTime"):"")+(statusExpireSeconds>0?(" and "+statusTimeField+" < :statusExpireTime"):"")
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
            Timestamp statusTime=new Timestamp(System.currentTimeMillis());
            if(updateStatus(m,QueueStatus.queue,statusTime,null,null,null)) {
                boolean suc=false;
                try {
                    synchronized (idsInQueue) {
                        id = getId(m);
                        suc = queue.add(new Message<>(m, id));
                        idsInQueue.add(id);
                    }
                } catch (Exception e) {
                    suc=false;
                }
                // 如果加入队列失败
                if(!suc) {
                    // 如果前一步锁定是成功的，那么这一步理论上是一定会成功的
                    suc = updateStatus(m,originalStatus,originalStatusTime,null,null,null);
                    if(!suc) {
                        RuntimeException ex=new RuntimeException("消息在恢复非锁定状态时失败");
                        Logger.exception(ex);
                        throw ex;
                    }
                }
            }
        }
        dao.resumePrintThreadSQL();

    }

    synchronized Message<M> poll() {
        Message<M> message=queue.poll();
        if(message!=null) {
            idsInQueue.remove(message.getId());
        }
        return message;
    }

    /**
     * 消费消息
     * */
    void consumeInternal(Message<M> message) {
        Timestamp statusTime=new Timestamp(System.currentTimeMillis());
        M messageBody=message.getMessage();
        M messageBodyInDB=refresh(messageBody);
        QueueStatus messageBodyStatus =BeanUtil.getFieldValue(messageBody,statusField,QueueStatus.class);
        QueueStatus messageBodyInDBStatus =BeanUtil.getFieldValue(messageBody,statusField,QueueStatus.class);
        if(messageBodyInDBStatus==QueueStatus.consumed) {
            return;
        }
        if(messageBodyInDBStatus!=messageBodyStatus) {
            return;
        }

        boolean suc=updateStatus(messageBody, QueueStatus.consuming,statusTime,null,null,null);
        Integer retrys=BeanUtil.getFieldValue(messageBody,retryField,Integer.class);
        if(suc) {
            try {
                retrys++;
                Result result=this.consume(messageBody);
                suc=updateStatus(messageBody,QueueStatus.consumed,statusTime,null,result,retrys);
                // 理论上不会失败，暂不处理
                if(!suc) {
                    throw new RuntimeException("异常-01");
                }
            } catch (Exception e) {
                suc=updateStatus(messageBody,QueueStatus.failure,statusTime,e,null,retrys);
                // 理论上不会失败，暂不处理
                if(!suc) {
                    throw new RuntimeException("异常-02");
                }
            }
        } else {
            // 理论上不会失败，暂不处理
            throw new RuntimeException("异常-03");
        }
    }

    protected abstract Result consume(M message) throws Exception;

    public void setDao(DAO dao) {
        this.dao = dao;
        this.validate();
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
        messageQueue.fetchIntoQueue();
    }


}

class Message<M extends Entity> {
    private String id;
    private M message;
    private int retrys=0;

    public Message(M message,String id) {
        this.message=message;

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

