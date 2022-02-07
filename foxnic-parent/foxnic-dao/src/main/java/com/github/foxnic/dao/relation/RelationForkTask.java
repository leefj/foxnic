package com.github.foxnic.dao.relation;

import com.github.foxnic.dao.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RelationForkTask<S extends Entity, T extends Entity> extends JoinForkTask<JoinResult> {

    private static final long serialVersionUID = 6748534103195740106L;

    /**
     * 用于存放要求和的数组
     */
    private final Collection<S> pos;

    private Class<S> poType;

    private Class<T> targetType;

    private PropertyRoute<S, T> route;

    private RelationSolver relationSolver;

    private String tid;

    private String tag;

    /**
     * 查分的粒度大小 这里将每一百个数据分为一组进行处理 如果超过100会在进一步分组
     */
    private long threshold = 36;

    public RelationForkTask(String tag,Object loginUserId, RelationSolver relationSolver, Class<S> poType, Collection<S> pos, Class<T> targetType, PropertyRoute<S, T> route, String tid) {
        super(loginUserId);
        this.relationSolver = relationSolver;
        this.pos = pos;
        this.threshold = route.getFork();
        this.poType = poType;
        this.route = route;
        this.targetType = targetType;
        this.tid = tid;
        this.tag=tag;

    }

    @Override
    protected JoinResult<S, T> compute() {

        //if(Logger.getTID()==null) {
        //	Logger.setTID(this.tid);
        //}

        // 如果个数在范围内，则计算
        if (this.pos.size() <= threshold) {
            return this.relationSolver.joinInFork(tag,poType, pos, route, targetType);
        }

        //一分为二
        List<S> leftPos = new ArrayList<>();
        List<S> rightPos = new ArrayList<>();
        int i = 0;
        for (S item : this.pos) {
            if (i < threshold) {
                leftPos.add(item);
            } else {
                rightPos.add(item);
            }
            i++;
        }

        //任务2
        RelationForkTask<S, T> rightTask = new RelationForkTask<>(this.tag,this.getLoginUserId(), this.relationSolver, poType, rightPos, targetType, route, this.tid);
        //任务1
        RelationForkTask<S, T> leftTask = new RelationForkTask<>(this.tag,this.getLoginUserId(), this.relationSolver, poType, leftPos, targetType, route, this.tid);

        invokeAll(rightTask,leftTask);

        JoinResult leftResult = leftTask.join();
        JoinResult rightResult = rightTask.join();

        return leftResult.merge(rightResult);


//        //任务2
//        RelationForkTask<S, T> rightTask = new RelationForkTask<>(this.getLoginUserId(), this.relationSolver, poType, rightPos, targetType, route, this.tid);
//        rightTask.fork();
//
//        //任务1
//        RelationForkTask<S, T> leftTask = new RelationForkTask<>(this.getLoginUserId(), this.relationSolver, poType, leftPos, targetType, route, this.tid);
//        //提交给了线程池去处理
//        JoinResult leftResult = leftTask.compute();
//        //拿出上面提交到线程池的结果
//        JoinResult rightResult = rightTask.join();
//        //最后进行sum返回
//        return leftResult.merge(rightResult);
    }
}
