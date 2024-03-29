package com.github.foxnic.dao.relation;

import com.github.foxnic.dao.entity.FieldsBuilder;

import java.util.*;

public class PropertyNameForkTask extends JoinForkTask<Map<String,JoinResult>> {

	private String[] propertyNames;
	private RelationSolver relationSolver;
	private Collection pos;
	private String tag;

	private  Map<String, FieldsBuilder> fieldsBuilderMap;

	public PropertyNameForkTask(String tag,Object loginUserId, RelationSolver relationSolver, Collection pos, String[] propertyNames,Map<String, FieldsBuilder> fieldsBuilderMap) {
		super(loginUserId);
		this.propertyNames =propertyNames;
		this.relationSolver=relationSolver;
		this.pos=pos;
		this.tag=tag;
		this.fieldsBuilderMap=fieldsBuilderMap;
	}


	@Override
	protected Map<String,JoinResult> compute() {

		// 执行
		if(propertyNames.length==1) {
			Map<String,JoinResult> map=new HashMap<>();
			JoinResult result=this.relationSolver.join(tag,pos,this.fieldsBuilderMap,propertyNames[0]);
			map.put(propertyNames[0],result);
			return map;
		}

	 	// Fork
		List<PropertyNameForkTask> tasks=new ArrayList<>();
		for (String propertyName : propertyNames) {
			PropertyNameForkTask task = new PropertyNameForkTask(this.tag,this.getLoginUserId(),this.relationSolver,this.pos, new String[] {propertyName},this.fieldsBuilderMap);
			tasks.add(task);
		}
		//调用
		invokeAll(tasks);
		// Join
		Map<String,JoinResult> results=new HashMap<>();
		for (PropertyNameForkTask task : tasks) {
			Map<String,JoinResult> leftResult = task.join();
			results.putAll(leftResult);
		}

		return  results;

//		String[] leftTypes=new String[] {this.propertyNames[0]};
//		String[] rightTypes=new String[this.propertyNames.length-1];
//		for (int i = 1; i < propertyNames.length; i++) {
//			rightTypes[i-1]=propertyNames[i];
//		}
//
//
//
//		//任务1
//        PropertyNameForkTask leftTask = new PropertyNameForkTask(this.getLoginUserId(),this.relationSolver,this.pos,leftTypes);
////        Map<String,JoinResult> leftResult = leftTask.compute();
//		leftTask.fork();
//		Map<String,JoinResult> leftResult = leftTask.join();
//
//		//任务2
//		PropertyNameForkTask rightTask = new PropertyNameForkTask(this.getLoginUserId(),this.relationSolver,this.pos,rightTypes);
//        rightTask.fork();
//        Map<String,JoinResult> rightResult = rightTask.join();
//
//        //
//        leftResult.putAll(rightResult);
//
//        return  leftResult;

	}

}
