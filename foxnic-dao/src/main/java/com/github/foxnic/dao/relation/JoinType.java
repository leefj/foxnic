package com.github.foxnic.dao.relation;

/**
 * Join 类型
 * */
public enum JoinType {
	
    JOIN("join","join"),LEFT_JOIN("left join","join"),RIGHT_JOIN("right join","join");
    
	private String join;
	private String revertJoin;
	
    private JoinType(String join,String revertJoin) {
    	this.join=join;
    	this.revertJoin=revertJoin;
    }

	public String getJoinSQL() {
		return join;
	}
    
}
