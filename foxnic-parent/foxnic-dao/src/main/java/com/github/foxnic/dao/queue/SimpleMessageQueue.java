package com.github.foxnic.dao.queue;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Expr;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 基于数据库的简单消息队列
 */
public abstract class SimpleMessageQueue<M extends Entity> {

    private DAO dao;
    private String table;
    private int ignoreSeconds;
    private String[] idFields;
    private String statusField;
    private String pushTimeField;
    private String retryField;
    //
    private String statusTimeField;
    private int statusExpireSeconds;
    //
    private SimpleTaskManager simpleTaskManager;
    private Class<M> messagePoType=null;

    private int threadCount=2;
    private int consumerCount=2;
    private long consumerCheckInterval=1000L;
    private int fetchSize=16;

    private LinkedBlockingQueue<SimpleMessage<M>> queue;
    private Long queueFetchTime;
    private List<Consumer> consumers;

    public SimpleMessageQueue(DAO dao, String table, String[] idFields, String statusField, String pushTimeField,String statusTimeField,String retryField,int ignoreSeconds,int statusExpireSeconds) {
        this.dao = dao;
        this.table = table;
        this.idFields = idFields;
        this.statusField = statusField;
        this.pushTimeField = pushTimeField;
        this.statusTimeField = statusTimeField;
        this.ignoreSeconds=ignoreSeconds;
        this.statusExpireSeconds=statusExpireSeconds;

        ParameterizedType type=(ParameterizedType)this.getClass().getGenericSuperclass();
        Type[] types=type.getActualTypeArguments();
        messagePoType=(Class)types[0];

        //TODO 增加字段校验逻辑
    }


    public SimpleMessageQueue(DAO dao, String table, String idField, String statusField, String pushTimeField,String statusTimeField,String retryField,int ignoreSeconds,int statusExpireSeconds) {
        this(dao, table, new String[]{idField}, statusField, pushTimeField,statusTimeField,retryField,ignoreSeconds,statusExpireSeconds);
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
            BeanUtil.setFieldValue(m,statusField,QueueStatus.waiting.code());
            BeanUtil.setFieldValue(m,pushTimeField,nowDate);
            BeanUtil.setFieldValue(m,statusTimeField,nowTime);
        }
        dao.insertEntities(messageList);
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


    boolean updateStatus (M message, QueueStatus status, Timestamp statusTime) {
        ConditionExpr conditionExpr=new ConditionExpr();
        return true;
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

        Expr select=new Expr();

        // 查询
        PagedList<M> messageList=dao.queryPagedEntities(messagePoType,fetchSize,1,select);
        for (M m : messageList) {
            // 如果锁定成功，则加入到队列
            QueueStatus originalStatus=BeanUtil.getFieldValue(m,statusField,QueueStatus.class);
            Timestamp originalStatusTime=BeanUtil.getFieldValue(m,statusTimeField,Timestamp.class);
            Timestamp statusTime=new Timestamp(System.currentTimeMillis());
            if(updateStatus(m,QueueStatus.queue,statusTime)) {
                boolean suc=false;
                try {
                    suc=queue.add(new SimpleMessage<>(m));
                } catch (Exception e) {
                    suc=false;
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

    }

    SimpleMessage<M> poll() {
        return queue.poll();
    }

    /**
     * 消费消息
     * */
    void consumeInternal(SimpleMessage<M> message) {
        Timestamp statusTime=new Timestamp(System.currentTimeMillis());
        M messageBody=message.getMessage();
        boolean suc=updateStatus(messageBody, QueueStatus.consuming,statusTime);
        if(suc) {
            try {
                this.consume(messageBody);
                suc=updateStatus(messageBody,QueueStatus.consumed,statusTime);
                if(!suc) {

                }
            } catch (Exception e) {
                suc=updateStatus(messageBody,QueueStatus.failure,statusTime);
                if(!suc) {

                }
            }
        } else {

        }
    }

    protected abstract void consume(M message);

}

class Consumer<M extends Entity> implements Runnable {


    private SimpleMessageQueue<M> messageQueue;

    public Consumer(SimpleMessageQueue<M> messageQueue){
        this.messageQueue=messageQueue;
    }

    @Override
    public void run() {

        while (true) {
            SimpleMessage message=messageQueue.poll();
            if(message==null) break;
            messageQueue.consumeInternal(message);
        }
        //
        messageQueue.fetchIntoQueue();
    }


}
