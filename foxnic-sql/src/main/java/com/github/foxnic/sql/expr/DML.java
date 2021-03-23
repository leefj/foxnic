package com.github.foxnic.sql.expr;

/**
 * DML语句，抽象类
 * @author fangjieli
 *
 */
public abstract class DML extends SubSQL {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5617635948263550796L;

	public Expr toExpr()
	{
		Expr se=Expr.create(this.getListParameterSQL(),this.getListParameters());
		return se;
	}
	
	
//	/**
//	 * 对字符串类型进行适当的转换
//	 * @param dao DAO
//	 * @param table 表
//	 * @param fld  字段
//	 * @param val 值
//	 * @return 转换后的值
//	 * */
//	public static Object castStringValue(DAO dao, String table, String fld, String val) {
//		DBColumnMeta colm=dao.getTableColumnMeta(table, fld);
//		if(colm==null)
//		{
//			return val;
////			throw new RuntimeException(table+"."+fld+"在数据库中不存在，可能不是一个有效字段");
//		}
//		DBDataType categery=colm.getDBDataType();
//		if(categery==null) {
//			return val;
//		}
//		Object casted=categery.cast(val);
//		if(casted==null)
//		{
//			throw new SQLValidateException("无法将 "+table+"."+fld+"设置为字符串值 "+val+", 要求为 "+categery.name()+" 类型");
//		}
//		
//		return casted;
//	}
 
	/**
	 * 加括号
	 * @param sub 语句
	 * @return 加了括号后的语句
	 * */
	protected String bracketSQL(String sub)
	{
		if(sub.length()>7)
		{
			String starts=sub.substring(0, 7).trim();
			if("select".equals(starts) || (!starts.startsWith("(") && sub.indexOf(" ")!=-1))
			{
				sub="("+sub+")";
			}
		}
		return sub;
	}
	
	private static final String CHARS="_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`\"";
	private static final String NUMS="0123456789";
	
	/**
	 * 检查变量合法性
	 * @param name 变量名
	 * @return 变量不合法则改写变量
	 * */
	protected static String checkSQLName(String name)
	{
		
		name=name.trim();
		String ch=name.substring(0,1);
		StringBuilder builder=new StringBuilder();
		if(CHARS.indexOf(ch)==-1)
		{
//			name="E\"["+name+"]\"";
			name=builder.append("E\"[").append(name).append("]\"").toString();

			return name;
		}
		for (int i = 1; i < name.length(); i++) {
			ch=name.substring(i, i+1);
			if(CHARS.indexOf(ch)==-1 &&  NUMS.indexOf(ch)==-1)
			{
//				name="E\"["+name+"]\"";
				//清空
				builder.setLength(0);
				name=builder.append("E\"[").append(name).append("]\"").toString();
				return name;
			}
		}
		return name;
	}
	
	public static void main(String[] args) {
		StringBuilder builder=new StringBuilder();
		builder.append("a").append("b");
		System.out.println(builder);
		builder.setLength(0);
		builder.append("as").append("bd");
		System.out.println(builder);
	} 
	
	protected boolean quotes=false;
 
	//如果是标识符，就不会有这些符号
	private static final char[] IDENTITY_EXCLUDE_CHARS= {'(',')',' ',',',';','+','-','*','/','%' ,'&','|','@',' ','\t','\r','\n'}; 
	
	protected String putInQuotes(String identity)
	{
		
		if(!quotes) return identity;
		//可能不是Identity的情况
		for (char c : IDENTITY_EXCLUDE_CHARS) {
			if(identity.indexOf(c)>-1) return identity;
		}
		identity=this.getSQLDialect().getDialectProcessor().quotes(identity);
		return identity;
	}
	
}
