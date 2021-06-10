package com.github.foxnic.dao.relation;

import com.github.foxnic.sql.meta.DBField;

public class Join {

//    private String sourceTable;
//    private List<String> sourceTableFields=new ArrayList<>();
    private JoinPoint sourceJoinPoint;

//    private String targetTable;
//    private List<String> targetTableFields=new ArrayList<>();
    private JoinPoint targetJoinPoint;

    private JoinType joinType=JoinType.JOIN;
    
    public Join from(JoinPoint sourceJoinPoint) {
    	  this.sourceJoinPoint=sourceJoinPoint;
    	  return this;
    }

    public void join(DBField... targetField) {
        this.targetJoinPoint=new JoinPoint(targetField);
        this.joinType=JoinType.JOIN;
    }

    public void leftJoin(DBField... targetField) {
    	 this.targetJoinPoint=new JoinPoint(targetField);
        this.joinType=JoinType.LEFT_JOIN;
    }

    public void rightJoin(DBField... targetField) {
    	 this.targetJoinPoint=new JoinPoint(targetField);
        this.joinType=JoinType.RIGHT_JOIN;
    }

	String getSourceTable() {
		return sourceJoinPoint.table().name();
	}

	public String getTargetTable() {
		return this.targetJoinPoint.table().name();
	}

	public JoinType getJoinType() {
		return joinType;
	}

	private Join revertJoin=null;
	
	public Join getRevertJoin() {
		if(revertJoin!=null) return revertJoin;
		revertJoin=new Join();
		revertJoin.sourceJoinPoint=this.targetJoinPoint;
		revertJoin.targetJoinPoint=this.sourceJoinPoint;
		revertJoin.joinType=JoinType.JOIN;
		return revertJoin;
	}

	private String[] sourceFields=null;
	public String[] getSourceFields() {
		if(sourceFields!=null) {
			return sourceFields;
		}
		sourceFields=new String[this.sourceJoinPoint.fields().length];
		for (int i = 0; i < sourceFields.length; i++) {
			sourceFields[i]=this.sourceJoinPoint.fields()[i].name();
		}
		return sourceFields;
	}
	
	
	private String[] targetFields=null;
	public String[] getTargetFields() {
		if(targetFields!=null) {
			return targetFields;
		}
		targetFields=new String[this.targetJoinPoint.fields().length];
		for (int i = 0; i < targetFields.length; i++) {
			targetFields[i]=this.targetJoinPoint.fields()[i].name();
		}
		return targetFields;
	}

	public JoinPoint getSourceJoinPoint() {
		return sourceJoinPoint;
	}

	public JoinPoint getTargetJoinPoint() {
		return targetJoinPoint;
	}

	@Override
	public String toString() {
		return this.sourceJoinPoint.toString()+"  <"+this.getJoinType().name()+">  "+this.targetJoinPoint.toString();
	}

}
