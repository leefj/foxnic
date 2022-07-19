package com.github.foxnic.sql.expr;


import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.meta.DBField;

import java.util.*;



/**
 *
 * in语句构造器
 * @author fangjieli
 *
 */
public class In extends SubSQL implements SQL,WhereWapper {


	private ArrayList<String> field=new ArrayList<>();
	public String getField() {
		if(field.size()==0) return null;
		return field.get(0);
	}

	public void setField(String... field) {
		this.field.clear();
		this.field.addAll(Arrays.asList(field));
		in=null;
	}

	private List items=new ArrayList<Object>();

	public List<Object> getItems() {
		return items;
	}

	public void setItems(ArrayList<? extends Object> items) {
		this.items = items;
		in=null;
	}

	public void addItem(Object... item) {
		if(item.length==1) {
			this.items.add(item[0]);
		} else {
			this.items.add(item);
		}
		in=null;
	}

	/**
	 * 转换好的Expr，如果有变化则置空，待需要时重新转换
	 * */
	private Expr in=null;

	private boolean not=false;

	public boolean isNot() {
		return not;
	}

	public In not()
	{
		this.not=true;
		return this;
	}

	/**
	 * 返回包含当前In语句的条件表达式
	 * */
	public ConditionExpr  toConditionExpr() {
		ConditionExpr ce=new ConditionExpr();
		ce.and(this);
		ce.setParent(this.parent());
		ce.setNameBeginIndex(this.getNameIndexBegin());
		return ce;
	}

	/**
	 * 转换成条件表达式
	 * */
	public Expr toExpr()
	{
		if(field.size()==0) return null;

		if(in!=null) {
			return in;
		}

		boolean single=field.size()==1;

		SQLStringBuilder sql=new SQLStringBuilder();
		if(not) sql.append(SQLKeyword.NOT);
		if(!single) {
			sql.append(SQLKeyword.LEFT_BRACKET);
		}

		for (String f : field) {
			sql.append(f).append(SQLKeyword.COMMA);
		}
		sql.deleteLastChar(1);

		if(!single) {
			sql.append(SQLKeyword.RIGHT_BRACKET);
		}
		sql.append(SQLKeyword.IN,SQLKeyword.LEFT_BRACKET);

		ArrayList<Object> ps=new ArrayList<>();

		for (Object object : items) {
			if(single) {
				ps.add(object);
			} else {
				Object[] arr=null;
				if(object instanceof Object[]) {
					arr=(Object[])object;
				}

				if(arr==null || arr.length!=field.size()) {
					throw new IllegalArgumentException("需要长度为"+field.size()+"的Object数组作为in语句的元素");
				}

				for (Object p : arr) {
					ps.add(p);
				}

			}
		}


		for (Object object : items) {
			if(!single)
			{
				sql.append(SQLKeyword.LEFT_BRACKET);
			}
			for (String f : field) {
				sql.append(SQLKeyword.QUESTION,SQLKeyword.COMMA);
			}
			sql.deleteLastChar(1);
			if(!single)
			{
				sql.append(SQLKeyword.RIGHT_BRACKET);
			}
			sql.append(SQLKeyword.COMMA);
		}

		if(items.size()>0)
		{
			sql.deleteLastChar(1);
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);

		in=new Expr(sql.toString(),ps.toArray());
		in.setNameBeginIndex(this.getNameIndexBegin());
		in.setParent(this.parent());
		return in;
	}

	public In()
	{}

	public In(DBField field, Object... items) {
		this(field.name(),items);
	}

	public In(String field,Object... items) {
		Utils.validateDBIdentity(field);
		this.field.add(field);
		for (Object object : items) {
			this.items.add(object);
		}
	}

	public In addField(String field)
	{
		Utils.validateDBIdentity(field);
		this.field.add(field);
		in=null;
		return this;
	}

	public In(DBField[] fields,Collection<? extends Object> items)
	{
		if(items==null) items=new ArrayList<>();
		for (DBField f : fields) {
			Utils.validateDBIdentity(f.name());
		}
		for (DBField f : fields) {
			this.field.add(f.name());
		}

		this.addArrayItems(items);

	}

	public In(String[] fields,Collection<? extends Object> items)
	{

		if(items==null) items=new ArrayList<>();
		for (String f : fields) {
			Utils.validateDBIdentity(f);
		}
		this.field.addAll(Arrays.asList(fields));

		this.addArrayItems(items);
	}

	private void addArrayItems(Collection<? extends Object> items) {
		for (Object object : items) {
			if(object==null) continue;
			if(object.getClass().isArray()) {
				Object[] arr=(Object[]) object;
				if(arr.length==0) continue;
				if(arr.length==1) {
					this.items.add(arr[0]);
				} else {
					this.items.add(object);
				}
			} else {
				this.items.add(object);
			}
		}
	}

	public In(DBField field,Collection<? extends Object> items) {
		this(field.name(),items);
	}

	public In(String field,Collection<? extends Object> items) {
		if(items==null) items=new ArrayList<>();
		Utils.validateDBIdentity(field);
		this.field.add(field);
		for (Object object : items) {
			this.items.add(object);
		}
	}

	@Override
	public String getSQL(SQLDialect dialect) {
		return toExpr().getSQL(dialect);
	}

	@Override
	public String getListParameterSQL() {
		return toExpr().getListParameterSQL();
	}

	@Override
	public Object[] getListParameters() {
		return toExpr().getListParameters();
	}

	@Override
	public String getNamedParameterSQL() {
		this.beginParamNameSQL();

		String sql=toExpr().getNamedParameterSQL();
		this.endParamNameSQL();
		return sql;
	}

	@Override
	public Map<String, Object> getNamedParameters() {
		this.beginParamNameSQL();
		Map<String, Object> param = toExpr().getNamedParameters();
		this.endParamNameSQL();
		return param;
	}

	/**
	 * 判断是否为空
	 * 集合元素个数为0 , 判定为 empty
	 * @return 是否空
	 * */
	@Override
	public boolean isEmpty() {
		if(this.items.isEmpty()) return true;
		Expr se=this.toExpr();
		if(se==null) return true;
		return se.isEmpty();
	}

	@Override
	public boolean isAllParamsEmpty() {
		for (Object p : items) {
			if(p==null) continue;
			if(p instanceof SQL)
			{
				SQL sql=(SQL)p;
				if(!sql.isAllParamsEmpty())
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public In clone() {
		In in=new In();
		in.field=this.field;
		in.items.addAll(this.items);
		if(this.in!=null) {
			in.in=this.in.clone();
		}
		in.not=this.not;
		return in;
	}

	/**
	 * 拆分In，使单个In语句包含较少的数据
	 * */
	public List<In> split(int count) {
		List<In> ins=new ArrayList<>();
		if(this.items.size()<=count) {
			ins.add(this);
		} else {
			List subs=new ArrayList<Object>();
			for (int i = 0; i < this.items.size(); i++) {
				subs.add(this.items.get(i));
				if(subs.size()==count) {
					In in=new In();
					in.setSQLDialect(this.getSQLDialect());
					in.field=this.field;
					in.items=subs;
					ins.add(in);
					subs=new ArrayList<Object>();
				}
			}
			if(subs.size()>0) {
				In in = new In();
				in.setSQLDialect(this.getSQLDialect());
				in.field = this.field;
				in.items = subs;
				ins.add(in);
			}
		}
		return  ins;
	}

}
