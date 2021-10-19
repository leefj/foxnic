package com.github.foxnic.dao.relation;

import java.util.*;

public class PropertyTypeForkTask  extends JoinForkTask<Map<String,JoinResult>> {

	private Class[] targetTypes;
	private RelationSolver relationSolver;
	private Collection pos;
	public PropertyTypeForkTask(Object loginUserId,RelationSolver relationSolver,Collection pos, Class[] targetTypes) {
		super(loginUserId);
		this.targetTypes=targetTypes;
		this.relationSolver=relationSolver;
		this.pos=pos;
	}


	@Override
	protected Map<String,JoinResult> compute() {

		// 执行
		if(targetTypes.length==1) {
			return this.relationSolver.join(pos,targetTypes[0]);
		}

		// Fork
		List<PropertyTypeForkTask> tasks=new ArrayList<>();
		for (Class targetType : targetTypes) {
			PropertyTypeForkTask task = new PropertyTypeForkTask(this.getLoginUserId(),this.relationSolver,this.pos,new Class[]{targetType});
			tasks.add(task);
		}
		// 调用
		invokeAll(tasks);
		// Join
		Map<String,JoinResult> results=new HashMap<>();
		for (PropertyTypeForkTask task : tasks) {
			Map<String,JoinResult> result = task.join();
			results.putAll(result);
		}
		return  results;

//		Class[] leftTypes=new Class[] {this.targetTypes[0]};
//		Class[] rightTypes=new Class[this.targetTypes.length-1];
//		for (int i = 1; i < targetTypes.length; i++) {
//			rightTypes[i-1]=targetTypes[i];
//		}
//
//		//任务1
//        PropertyTypeForkTask leftTask = new PropertyTypeForkTask(this.getLoginUserId(),this.relationSolver,this.pos,leftTypes);
//        Map<String,JoinResult> leftResult = leftTask.compute();
//
//		//任务2
//		PropertyTypeForkTask rightTask = new PropertyTypeForkTask(this.getLoginUserId(),this.relationSolver,this.pos,rightTypes);
//        rightTask.fork();
//        Map<String,JoinResult> rightResult = rightTask.join();
//
//        //
//        leftResult.putAll(rightResult);
//
//        return  leftResult;

	}

}
