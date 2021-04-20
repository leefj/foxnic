package com.github.foxnic.dao.relation;

import java.util.Collection;
import java.util.Map;

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
		
		if(targetTypes.length==1) {
			return this.relationSolver.join(pos,targetTypes[0]);
		}
		
		
		Class[] leftTypes=new Class[] {this.targetTypes[0]};
		Class[] rightTypes=new Class[this.targetTypes.length-1];
		for (int i = 1; i < targetTypes.length; i++) {
			rightTypes[i-1]=targetTypes[i];
		}
		
		//任务1
        PropertyTypeForkTask leftTask = new PropertyTypeForkTask(this.getLoginUserId(),this.relationSolver,this.pos,leftTypes);
        Map<String,JoinResult> leftResult = leftTask.compute();
		
		//任务2
		PropertyTypeForkTask rightTask = new PropertyTypeForkTask(this.getLoginUserId(),this.relationSolver,this.pos,rightTypes);
        rightTask.fork();
        Map<String,JoinResult> rightResult = rightTask.join();
        
        //
        leftResult.putAll(rightResult);
 
        return  leftResult;
 
	}

}
