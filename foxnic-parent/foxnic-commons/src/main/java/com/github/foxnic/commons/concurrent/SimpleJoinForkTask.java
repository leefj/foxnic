package com.github.foxnic.commons.concurrent;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SimpleJoinForkTask<IN,OUT>  extends RecursiveTask<List<OUT>> {

    private static ForkJoinPool POOL = new ForkJoinPool();

    public static interface TaskExecutor<IN,OUT> {
        List<OUT> doTask(List<IN> input);
    }


    private List<IN> input;
    private int batchSize;
    private TaskExecutor<IN,OUT> executor;

    public SimpleJoinForkTask(List<IN> input,int batchSize) {
        this.input=input;
        this.batchSize=batchSize;
    }

    private SimpleJoinForkTask(List<IN> input,int batchSize,TaskExecutor<IN,OUT> executor) {
        this.input=input;
        this.batchSize=batchSize;
        this.executor=executor;
    }

    private List<OUT> executeInternal(List<IN> input) {
        return executor.doTask(input);
    }

    public List<OUT> execute(final TaskExecutor<IN,OUT> executor) {
        this.executor=executor;
        return POOL.invoke(this);
    }

    @Override
    protected List<OUT> compute() {

        //实际的分解执行
        if(input.size()<=batchSize) {
            return executeInternal(input);
        }

        List<IN> lefts=input.subList(0,batchSize);
        List<IN> rights=input.subList(batchSize,input.size());


        //任务1
        SimpleJoinForkTask<IN,OUT> leftTask = new SimpleJoinForkTask<>(lefts,batchSize,executor);
        //任务2
        SimpleJoinForkTask<IN,OUT> rightTask = new SimpleJoinForkTask<>(rights,batchSize,executor);

        invokeAll(leftTask,rightTask);

        List<OUT> leftResult = leftTask.join();
        List<OUT> rightResult=rightTask.join();

        leftResult.addAll(rightResult);

        return  leftResult;
    }
}
