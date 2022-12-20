package com.github.foxnic.dao.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.GlobalSettings;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLBuilder;
import com.github.foxnic.sql.data.DataNameFormat;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.meta.DBField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.*;

/**
 * 记录
 * @author fangjieli
 *
 */
public class Rcd  implements ExprRcd,Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6102465601794959457L;


	private RcdSet ownerSet;

	/**
	 * @return  获得记录所属的记录集
	 * */
	public RcdSet getOwnerSet() {
		return ownerSet;
	}

	private DataNameFormat dataNameFormat=null;

	/**
	 * 设置JSON的属性格式；
	 * 若当前对象有设置，则返回当前对对象的属性值；
	 * 若当前对象无设置，则返回所属集合的设置；
	 * 若无所属集合，则返回全局默认设置 AbstractSet.DEFAULT_DATA_NAME_FORMAT；
	 * @return 格式化方式
	 * */
	public DataNameFormat getDataNameFormat() {
		if(this.dataNameFormat==null)
		{
			return this.ownerSet==null?GlobalSettings.DEFAULT_DATA_NAME_FORMAT:this.ownerSet.getDataNameFormat();
		}
		else
		{
			return dataNameFormat;
		}
	}

	/**
	 * 设置JSON的属性格式；
	 * 若当前对象有设置，则返回当前对对象的该属性值；
	 * 若当前对象无设置，则返回所属集合的设置；
	 * 若无所属集合，则返回全局默认设置 AbstractSet.DEFAULT_DATA_NAME_FORMAT；
	 * @param dataNameFormat 格式化方式
	 * */
	public void setDataNameFormat(DataNameFormat dataNameFormat) {
		this.dataNameFormat = dataNameFormat;
	}

	/**
	 * 获得字段清单
	 * @return 字段清单
	 * */
	public List<String> getFields()
	{
		return this.ownerSet.getFields();
	}

	ArrayList<Object> values = null;
	ArrayList<Integer> dirtyFields= null;

	ArrayList<Expr> seValues = null;

	/**
	 * 设置SQL表达式值，用于数据保存
	 * @param i 	字段序号
	 * @param se	表达式
	 * @param ps 表达式参数
	 * @return 当前对象
	 * */
	public Rcd setExpr(int i,String se,Object... ps)
	{
		if(seValues==null)
		{
			seValues=new ArrayList<Expr>();
			for (Object v : values) {
				seValues.add(null);
			}
		}

		Expr ose=seValues.get(i);
		Expr nse=new Expr(se,ps);
		seValues.set(i, nse);

		String nseSQL=nse.getSQL();
		String oseSQL=null;
		if(ose!=null) oseSQL=ose.getSQL();

		//判断是否脏
		validateDirty(i,nseSQL,oseSQL);

		return this;
	}


	private void validateDirty(int i, Object ov, Object nv) {
		boolean dirty=false;
		if((ov==null && nv!=null) || (ov!=null && nv==null))
		{
			dirty=true;
		}
		else if(ov!=null && nv!=null)
		{
			dirty=!ov.equals(nv);
		}

		if(!dirtyFields.contains(i) && dirty)
		{
			dirtyFields.add(i);
		}
	}

	/**
	 * 设置SQL表达式值，用于数据保存
	 * @param field 	字段名称
	 * @param se	 值
	 * @param ps 表达式参数
	 * @return 当前对象
	 * */
	public Rcd setExpr(String field,String se,Object... ps)
	{
		int i = ownerSet.getMetaData().name2index(field);
		if(i==-1) {
			return null;
//			throw new NoFieldException(field);
		}
		return setExpr(i,se,ps);
	}

	/**
	 * 设置SQL表达式值，用于数据保存
	 * @param i 	字段名称
	 * @return 表达式
	 * */
	public Expr getExpr(int i)
	{
		if(seValues==null) return null;
		return seValues.get(i);
	}

	/**
	 * 设置SQL表达式值，用于数据保存
	 * @param field 	字段名称
	 * @return 表达式
	 * */
	public Expr getExpr(String field)
	{
		int i = ownerSet.getMetaData().name2index(field);
		if(i==-1) {
			return null;
//			throw new NoFieldException(field);
		}
		return getExpr(i);
	}


	private SaveAction nextSaveAction=SaveAction.NONE;

	/**
	 * 执行Save方法时的动作
	 * @return SaveAction
	 * */
	public SaveAction getNextSaveAction() {
		return nextSaveAction;
	}

	/**
	 * 设置保存是需要执行的动作，通常无需设置，由框架自行判断
	 * @param nextSaveAction SaveAction
	 * */
	public void setNextSaveAction(SaveAction nextSaveAction) {
		this.nextSaveAction = nextSaveAction;
	}

	/**
	 * @param ownerSet 隶属的记录集
	 * */
	public Rcd(RcdSet ownerSet) {
		this.ownerSet = ownerSet;
		this.values = new ArrayList<>(this.ownerSet.getMetaData().getColumnCount());
		this.dirtyFields = new ArrayList<>(this.ownerSet.getMetaData().getColumnCount());
		for (int i = 0; i < ownerSet.getMetaData().getColumnCount(); i++) {
			values.add(null);
		}
	}

	/**
	 * 清除脏数据
	 * */
	public void clearDitryFields()
	{
		dirtyFields.clear();
	}

	/**
	 * 判断记录内的所有值是否为 null
	 * @return 是否为空
	 * */
	public boolean isEmpty()
	{
		boolean empty=true;
		for (Object v : values) {
			if(v!=null)
			{
				empty=false;
				break;
			}
		}
		return empty;
	}

	private HashMap<Integer,ArrayList<Object>> versions=null;

	private void setVersioningValue(int index,Object value)
	{
		if(versions==null) versions=new HashMap<Integer,ArrayList<Object>>();
		ArrayList<Object> vs=versions.get(index);
		if(vs==null) {
			vs=new ArrayList<Object>();
			versions.put(index,vs);
		}
		vs.add(value);
	}

	/**
	 * 获得最初从数据库查询获得的数据，不受set方法影响
	 * @param <T> 类型
	 * 	@param field 字段名
	 * @param type 指定返回值类型
	 * @return 获得指定类型的原始值
	 * */
	public <T> T getOriginalValue(String field,Class<T> type)
	{
		int index=this.getOwnerSet().getMetaData().name2index(field);
		if(index==-1) {
			return null;
//			throw new NoFieldException(field);
		}
		Object ov=getOriginalValue(index);
		return DataParser.parse(type, ov);
	}

	/**
	 * 获得最初从数据库查询获得的数据，不受set方法影响
	 * @param <T> 类型
	 * @param type 指定返回值类型
	 * @param index 字段索引位置
	 * @return 获得指定类型的原始值
	 * */
	public <T> T getOriginalValue(int index,Class<T> type)
	{
		Object ov=getOriginalValue(index);
		return DataParser.parse(type, ov);
	}

	/**
	 * 获得最初从数据库查询获得的数据，不受set方法影响
	 * @param field 字段名
	 * @return 获得指定类型的原始值
	 * */
	public Object getOriginalValue(DBField field) {
		return getOriginalValue(field.name());
	}


	/**
	 * 获得最初从数据库查询获得的数据，不受set方法影响
	 * @param field 字段名
	 * @return 获得指定类型的原始值
	 * */
	public Object getOriginalValue(String field)
	{
		int index=this.getOwnerSet().getMetaData().name2index(field);
		if(index==-1) {
			return null;
//			throw new NoFieldException(field);
		}
		return getOriginalValue(index);
	}

	/**
	 * 获得最初从数据库查询获得的数据，不受set方法影响
	 * @param index 字段索引位置
	 * @return 获得原始值
	 * */
	public Object getOriginalValue(int index)
	{
		Object ov=null;
		if(versions!=null) {
			ArrayList<Object> vs=versions.get(index);
			if(vs!=null) ov=vs.get(0);
		}

		if(ov==null) {
			ov=this.getValue(index);
		}
		return ov;
	}

	/**
	 * 只对内部开放
	 */
	Rcd setValueInternal(int i, Object v) {

		Object ov=values.get(i);
		values.set(i, v);
		if(seValues!=null) seValues.set(i, null);

		//判断是否脏
		validateDirty(i, ov, v);

		return this;
	}

	/**
	 * 判断指定的字段是否为脏数据，设置之后数据变脏，保存到数据库后重置该值为false
	 * @param i 字段序号
	 * @return 是否脏
	 * */
	public boolean isDirty(int i)
	{
		return dirtyFields.contains(i);
	}

	/**
	 * 判断指定的字段是否为脏数据，设置之后数据变脏，保存到数据库后重置该值为false
	 * @param field 字段名称
	 * @return 是否脏
	 * */
	public boolean isDirty(DBField field) {
		return isDirty(field.name());
	}


	/**
	 * 判断指定的字段是否为脏数据，设置之后数据变脏，保存到数据库后重置该值为false
	 * @param field 字段名称
	 * @return 是否脏
	 * */
	public boolean isDirty(String field)
	{
		int i = ownerSet.getMetaData().name2index(field);
		if(i==-1) {
			return false;
//			throw new NoFieldException(field);
		}
		return dirtyFields.contains(i);
	}

	/**
	 * 获得值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Object getValue(int i) {
		return this.values.get(i);
	}

	/**
	 * 获得值
	 * @param field 字段名称
	 * @param def  字段值为null时的默认值
	 * @return 值
	 * */
	public Object getValue(String field, Object def) {
		Object val = getValue(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	public Object getValue(DBField field) {
		return this.getValue(field.name());
	}

	/**
	 * 获得值
	 * @param field 字段名称
	 * @return 值
	 * */
	public Object getValue(String field) {
		int i = ownerSet.getMetaData().name2index(field);
		if(i==-1) {
			return null;
//			throw new NoFieldException(field);
		}
		return i == -1 ? null : this.getValue(i);
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Character getChar(String field, Character def) {
		Character val = getChar(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Character getChar(DBField field) {
		return DataParser.parseChar(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Character getChar(String field) {
		return DataParser.parseChar(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Character getChar(int i, Character def) {
		Character val = getChar(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Character getChar(int i) {
		return DataParser.parseChar(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public String getString(int i, String def) {
		String val = getString(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 转换枚举值
	 *
	 * @param <T> ，枚举类型
	 * @param i 字段序号
	 * @param enumType 指定转换后的枚举类型
	 * @param defaultValue 转换失败时的默认值
	 * @param compareProperty 用于识别、比较的属性，null时用name()比较(忽略大小写)；指定改参数时，使用该属性上的值识别、比较
	 * @return the 枚举类型
	 */
	public <T extends Enum> T getEnum(int i, Class<? extends T> enumType, T defaultValue,String compareProperty) {
		return DataParser.parseEnum(this.getValue(i), enumType, defaultValue, compareProperty);
	}


	/**
	 * 转换枚举值
	 *
	 * @param <T> ，枚举类型
	 * @param field 字段
	 * @param enumType 指定转换后的枚举类型
	 * @param defaultValue 转换失败时的默认值
	 * @param compareProperty 用于识别、比较的属性，null时用name()比较(忽略大小写)；指定改参数时，使用该属性上的值识别、比较
	 * @return the 枚举类型
	 */
	public <T extends Enum> T getEnum(String field, Class<? extends T> enumType, T defaultValue,String compareProperty) {
		return DataParser.parseEnum(this.getValue(field), enumType, defaultValue, compareProperty);
	}




	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public String getString(String field, String def) {
		String val = getString(field);
		if (val == null) {
			val = def;
		}
		return val;
	}



	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public String getString(String field) {
		return DataParser.parseString(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public String getString(DBField field) {
		return DataParser.parseString(getValue(field.name()));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public String getString(int i) {
		return DataParser.parseString(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Boolean getBoolean(int i, Boolean def) {
		Boolean val = getBoolean(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Boolean getBoolean(String field, Boolean def) {
		Boolean val = getBoolean(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Boolean getBoolean(String field) {
		return DataParser.parseBoolean(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Boolean getBoolean(DBField field) {
		return DataParser.parseBoolean(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Boolean getBoolean(int i) {
		return DataParser.parseBoolean(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Byte getByte(int i, Byte def) {
		Byte val = getByte(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Byte getByte(String field, Byte def) {
		Byte val = getByte(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Byte getByte(String field) {
		return DataParser.parseByte(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Byte getByte(int i) {
		return DataParser.parseByte(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Short getShort(int i, Short def) {
		Short val = getShort(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Short getShort(String field, Short def) {
		Short val = getShort(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Short getShort(String field) {
		return DataParser.parseShort(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Short getShort(int i) {
		return DataParser.parseShort(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Integer getInteger(int i, Integer def) {
		Integer val = getInteger(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Integer getInteger(String field, Integer def) {
		Integer val = getInteger(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Integer getInteger(String field) {
		return DataParser.parseInteger(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Integer getInteger(DBField field) {
		return DataParser.parseInteger(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Integer getInteger(int i) {
		return DataParser.parseInteger(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Long getLong(int i, Long def) {
		Long val = getLong(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Long getLong(String field, Long def) {
		Long val = getLong(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Long getLong(String field) {
		return DataParser.parseLong(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Long getLong(DBField field) {
		return DataParser.parseLong(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Long getLong(int i) {
		return DataParser.parseLong(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public BigInteger getBigInteger(int i, BigInteger def) {
		BigInteger val = getBigInteger(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public BigInteger getBigInteger(String field, BigInteger def) {
		BigInteger val = getBigInteger(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public BigInteger getBigInteger(String field) {
		return DataParser.parseBigInteger(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public BigInteger getBigInteger(DBField field) {
		return DataParser.parseBigInteger(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public BigInteger getBigInteger(int i) {
		return DataParser.parseBigInteger(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Float getFloat(int i, Float def) {
		Float val = getFloat(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Float getFloat(String field, Float def) {
		Float val = getFloat(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Float getFloat(String field) {
		return DataParser.parseFloat(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Float getFloat(DBField field) {
		return DataParser.parseFloat(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Float getFloat(int i) {
		return DataParser.parseFloat(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Double getDouble(int i, Double def) {
		Double val = getDouble(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Double getDouble(String field, Double def) {
		Double val = getDouble(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Double getDouble(String field) {
		return DataParser.parseDouble(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Double getDouble(DBField field) {
		return DataParser.parseDouble(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Double getDouble(int i) {
		return DataParser.parseDouble(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public BigDecimal getBigDecimal(int i, BigDecimal def) {
		BigDecimal val = getBigDecimal(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public BigDecimal getBigDecimal(String field, BigDecimal def) {
		BigDecimal val = getBigDecimal(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public BigDecimal getBigDecimal(String field) {
		return DataParser.parseBigDecimal(getValue(field));
	}


	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public BigDecimal getBigDecimal(DBField field) {
		return DataParser.parseBigDecimal(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public BigDecimal getBigDecimal(int i) {
		return DataParser.parseBigDecimal(getValue(i));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @param def  默认值
	 * @return 值
	 * */
	public Date getDate(int i, Date def) {
		Date val = getDate(i);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @param def  默认值
	 * @return 值
	 * */
	public Date getDate(String field, Date def) {
		Date val = getDate(field);
		if (val == null) {
			val = def;
		}
		return val;
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Date getDate(String field) {
		return DataParser.parseDate(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public Date getDate(DBField field) {
		return DataParser.parseDate(getValue(field));
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public Date getDate(int i) {
		return DataParser.parseDate(getValue(i));
	}


	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public JSONObject getJSONObject(DBField field) {
		return this.getJSONObject(field.name());
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public JSONObject getJSONObject(String field) {
		Object value=this.getValue(field);
		if(value==null) return null;
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		} else {
			return JSONObject.parseObject(this.getString(field));
		}
	}

	/**
	 * 获得指定类型的值
	 * @param i 字段序号
	 * @return 值
	 * */
	public JSONObject getJSONObject(int i) {

		Object value=this.getValue(i);
		if(value==null) return null;

		if (value instanceof JSONObject) {
			return (JSONObject) value;
		} else {
			return JSONObject.parseObject(this.getString(i));
		}
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public JSONArray getJSONArray(DBField field) {
		return getJSONArray(field.name());
	}

	/**
	 * 获得指定类型的值
	 * @param field 字段
	 * @return 值
	 * */
	public JSONArray getJSONArray(String field) {

		Object value=this.getValue(field);
		if(value==null) return null;

		if (value instanceof JSONArray) {
			return (JSONArray) value;
		} else {
			return JSONArray.parseArray(this.getString(field));
		}
	}

	public JSONArray getJSONArray(int i) {

		Object value=this.getValue(i);
		if(value==null) return null;

		if (value instanceof JSONArray) {
			return (JSONArray) value;
		} else {
			return JSONObject.parseArray(this.getString(i));
		}
	}



	/**
	 * 按字段标签生成 JsonObject
	 * @return JSONObject
	 * */
	public JSONObject toJSONObject()
	{
		 return toJSONObject(null, null);
	}

	/**
	 * 按字段标签生成 JsonObject
	 * @param fields 需要包含的数据字段
	 * @return 需要包含的数据字段
	 * */
	public JSONObject toJSONObject(String... fields)
	{
		return toJSONObject(Arrays.asList(fields),null);
	}

	private Object getFormattedValue(int i)
	{
		Object v=this.getValue(i);

		if(v!=null && v.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			v=DataParser.parseTimestamp(v);
		}

		if(v instanceof Clob) {
			v=StringUtil.toString((Clob)v);
		} else if(v instanceof Timestamp) {
			v=DateUtil.format((Timestamp)v, "yyyy-MM-dd hh:mm:ss.SSSSSS");
		} else if(v instanceof Date) {
			v=DateUtil.format((Date)v, "yyyy-MM-dd hh:mm:ss");
		}
		return v;
	}

	/**
	 * 按字段标签生成 JsonObject,将includeFields包含字段，且excludeFields不包含的字段生成 JSONObject<br>
	 * 当 includeFields 为 null 时默认为全部字段<br>
	 * 当 excludeFields 为 null 时，默认无字段
	 * @param includeFields  将要包含的字段
	 * @param excludeFields  将要去除的字段
	 * @return 需要包含的数据字段
	 * */
	public JSONObject toJSONObject(List<String> includeFields,List<String> excludeFields) {
		return toJSONObject(includeFields, excludeFields, true);
	}

	/**
	 * 按字段标签生成 JsonObject,将includeFields包含字段，且excludeFields不包含的字段生成 JSONObject<br>
	 * 当 includeFields 为 null 时默认为全部字段<br>
	 * 当 excludeFields 为 null 时，默认无字段
	 * @param includeFields  将要包含的字段
	 * @param excludeFields  将要去除的字段
	 * @param format  是否格式化数据
	 * @return 需要包含的数据字段
	 * */
	public JSONObject toJSONObject(List<String> includeFields,List<String> excludeFields,boolean format)
	{
		QueryMetaData meta=this.ownerSet.getMetaData();
		JSONObject json=new JSONObject();

		String columnLabel=null;
		String columnLabelUpper=null;
		String columnLabelLower=null;
		String columnLabelConverted=null;
		if(includeFields!=null && excludeFields!=null)
		{
			for(int i=0;i<meta.getColumnCount();i++)
			{
				columnLabel=meta.getColumnLabel(i);
				columnLabelUpper=columnLabel.toLowerCase();
				columnLabelLower=columnLabel.toUpperCase();
				columnLabelConverted=AbstractSet.convertDataName(columnLabel,this.getDataNameFormat());
				//加入includeFields包含，且excludeFields不包含的字段
				if((includeFields.contains(columnLabelConverted) || includeFields.contains(columnLabel) || includeFields.contains(columnLabelUpper) || includeFields.contains(columnLabelLower) )
						&&  (!excludeFields.contains(columnLabelConverted) && !excludeFields.contains(columnLabel) && !excludeFields.contains(columnLabelUpper) && !excludeFields.contains(columnLabelLower)) )
				{
					json.put(columnLabelConverted,format?this.getFormattedValue(i):this.getValue(i));
				}
			}
		}
		else if(includeFields==null && excludeFields!=null)
		{
			for(int i=0;i<meta.getColumnCount();i++)
			{
				columnLabel=meta.getColumnLabel(i);
				columnLabelUpper=columnLabel.toLowerCase();
				columnLabelLower=columnLabel.toUpperCase();
				columnLabelConverted=AbstractSet.convertDataName(columnLabel,this.getDataNameFormat());
				//加入 excludeFields 不包含的字段
				if(!excludeFields.contains(columnLabelConverted) && !excludeFields.contains(columnLabel) && !excludeFields.contains(columnLabelUpper) && !excludeFields.contains(columnLabelLower))
				{
					json.put(columnLabelConverted,format?this.getFormattedValue(i):this.getValue(i));
				}
			}
		}
		else if(includeFields!=null && excludeFields==null)
		{
			for(int i=0;i<meta.getColumnCount();i++)
			{
				columnLabel=meta.getColumnLabel(i);
				columnLabelUpper=columnLabel.toLowerCase();
				columnLabelLower=columnLabel.toUpperCase();
				columnLabelConverted=AbstractSet.convertDataName(columnLabel,this.getDataNameFormat());
				//加入includeFields包含的字段
				if((includeFields.contains(columnLabelConverted) || includeFields.contains(columnLabel)  || includeFields.contains(columnLabelUpper)  || includeFields.contains(columnLabelLower)))
				{
					json.put(columnLabelConverted,format?this.getFormattedValue(i):this.getValue(i));
				}
			}
		}
		else if(includeFields==null && excludeFields==null)
		{
			for(int i=0;i<meta.getColumnCount();i++)
			{
				columnLabel=meta.getColumnLabel(i);
				columnLabelConverted=AbstractSet.convertDataName(columnLabel,this.getDataNameFormat());
				//加入全部字段
				json.put(columnLabelConverted,format?this.getFormattedValue(i):this.getValue(i));
			}
		}

		return json;
	}



	/**
	 * 按字段查询顺序生成 JsonArray
	 * @return JSONArray
	 * */
	public JSONArray toJSONArray()
	{
		JSONArray json=new JSONArray();
		for(Object value:this.values)
		{
			json.add(value);
		}
		return json;
	}

	@Override
	public String toString() {

		String str="RAW : "+this.toJSONObject().toJSONString();

		if(seValues!=null)
		{
			Expr seValue=null;
			String cName=null;
			JSONObject json=new JSONObject();
			for (int i = 0; i < values.size(); i++) {
				cName=this.getOwnerSet().getMetaData().getColumnLabel(i);
				seValue=getExpr(i);
				if(seValue!=null)
				{
					json.put(cName, seValue.getSQL());
				}
			}
			str+="\nExpr : "+json.toJSONString();
		}

		String[] tables=this.getOwnerSet().getMetaData().getDistinctTableNames();
		String table=null;
		if(tables!=null && tables.length>=1)
		{
			table=tables[0];
		}
		DAO dao = getDefaultDAO();

		if(dao!=null && StringUtil.hasContent(table))
		{
			try {
				str += "\nInsert SQL : " + SQLBuilder.buildInsert(this, table, dao, true);
			} catch (Exception e)  {}
			try {
				str+="\nUpdate SQL(All) : "+SQLBuilder.buildUpdate(this, SaveMode.ALL_FIELDS, table, dao);
			} catch (Exception e)  {}
			try {
				str+="\nUpdate SQL(Dirty) : "+SQLBuilder.buildUpdate(this, SaveMode.DIRTY_FIELDS, table, dao);
			} catch (Exception e)  {}
			try {
				str+="\nUpdate SQL(Not Null) : "+SQLBuilder.buildUpdate(this, SaveMode.NOT_NULL_FIELDS, table, dao);
			} catch (Exception e)  {}
			try {
				str+="\nDelete SQL : "+SQLBuilder.buildDelete(this,table, dao);
			} catch (Exception e)  {}
		}
		else
		{
//			str+="\nInsert SQL : "+"错误，缺少表名或DAO";
//			str+="\nUpdate SQL(All) : "+"错误，缺少表名或DAO";
//			str+="\nUpdate SQL(Dirty) : "+"错误，缺少表名或DAO";
//			str+="\nUpdate SQL(Not Null) : "+"错误，缺少表名或DAO";
//			str+="\nDelete SQL : "+"错误，缺少表名或DAO";
			return this.toJSONObject().toString();
		}




		return str;

	}



	/**
	 * 把记录转换为实体
	 * 支持Tity的实体类型以及POJO类型
	 * @param <T> 类型
	 * @param clazz 类型
	 * @return Entity
	 * */
	public <T> T toEntity(Class<T> clazz)
	{
		try {
			return toPOJOEntity(clazz);
		} catch (Exception e) {
			Logger.error("实体转换失败",e);
		}
		return null;
	}

	/**
	 * 把记录转换为实体
	 * 支持Tity的实体类型以及POJO类型
	 * @param <T> 类型
	 * @param clazz 类型
	 * @return POJO
	 * */
	private <T> T toPOJOEntity(Class<T> clazz) throws Exception {
		if(!EntityContext.isProxyType(clazz) && Entity.class.isAssignableFrom(clazz)) {
			clazz=EntityContext.getProxyType((Class)clazz);
		}
		T e=(T)BeanUtil.create(clazz);
		boolean read=false;
		if(e instanceof Entity) {
			Entity entity=(Entity)e;
			read = entity.read(this,true);
			if(read) {
				entity.clearModifies();
			}
		}
		if(!read) {
			QueryMetaData meta = this.ownerSet.getMetaData();
			String columnLabel = null;
			for (int i = 0; i < meta.getColumnCount(); i++) {
				columnLabel = meta.getColumnLabel(i);
				BeanUtil.setFieldValue(e, columnLabel, this.getValue(i));
			}
			if(e instanceof Entity) {
				((Entity)e).clearModifies();
			}
		}
		return e;
	}

	/**
	 * 设置值,与 setValue 等同
	 * @param i 字段位置
	 * @param value 值
	 * @return 当前对象
	 * */
	public Rcd set(int i,Object value)
	{
		return setValue(i,value);
	}

	/**
	 * 设置值,与 setValue 等同
	 * @param field 字段
	 * @param value 值
	 * @return 当前对象
	 * */
	public Rcd set(String field,Object value)
	{
		return setValue(field,value);
	}

	/**
	 * 设置值
	 * @param i 字段位置
	 * @param v 值
	 * @return 当前对象
	 * */
	public Rcd setValue(int i,Object v)
	{
		//处理枚举类型
		if(v instanceof Enum) {
			v=((Enum)v).name();
		}

		Object ov=this.getValue(i);
		 setValueInternal(i, v);
		//设置版本数据
		this.setVersioningValue(i, ov);
		return	this;
	}

	/**
	 * 设置值,与 set 等同
	 * @param field 字段
	 * @param v 值
	 * @return 当前对象
	 * */
	public Rcd setValue(String field,Object v)
	{
		int i=ownerSet.getMetaData().name2index(field);
		if(i==-1)
		{
			return null;
//			throw new NoFieldException(field);
		}
		return this.setValue(i, v);
	}


	void removeColumn(int i) {
		values.remove(i);
	}

	void addValue(Object value) {
		 this.values.add(value);
	}

	/**
	 * 克隆自己
	 * @return 返回克隆的新记录
	 * */
	public Rcd clone()
	{
		Rcd  r=new Rcd(this.ownerSet);
		for (int i = 0; i < this.values.size(); i++) {
			r.setValue(i, this.getValue(i));
		}
		return r;
	}


	/**
	 * 插入当前记录到指定库，指定表
	 * @param dao dao
	 * @param table 数据表
	 * @return 插入是否成功
	 * */
	public boolean insert(DAO dao,String table)
	{
		 return dao.insertRecord(this, table);
	}

	/**
	 * 插入数据到来源库，指定表
	 * @param table 数据表
	 * @return 插入是否成功
	 * */
	public boolean insert(String table)
	{
		DAO dao = getDefaultDAO();
		 return dao.insertRecord(this, table);
	}

	/**
	 * 插入数据到来源库，默认表
	 * @return 插入是否成功
	 * */
	public boolean insert()
	{
		DAO dao = getDefaultDAO();
		 return dao.insertRecord(this);
	}



	/**
	 * 从指定库删除数据，指定表
	 * @param dao dao
	 * @param table 数据表
	 * @return 删除是否成功
	 * */
	public boolean delete(DAO dao,String table)
	{
		 return dao.deleteRecord(this, table);
	}

	/**
	 * 从来源库删除数据，指定表
	 * @param table 数据表
	 * @return 删除是否成功
	 * */
	public boolean delete(String table)
	{
		DAO dao = getDefaultDAO();
		 return dao.deleteRecord(this, table);
	}

	/**
	 * 从来源库删除数据，默认表
	 * @return 删除是否成功
	 * */
	public boolean delete()
	{
		DAO dao = getDefaultDAO();
		 return dao.deleteRecord(this);
	}



	/**
	 * 更新数据到指定库，指定表和保存模式
	 * @param dao DAO
	 * @param table 表名
	 * @param saveMode 保存模式
	 * @return 逻辑值
	 * */
	public boolean update(DAO dao,String table,SaveMode saveMode)
	{
		 return  dao.updateRecord(this, table, saveMode);
	}

	/**
	 * 更新数据到来源库，指定表和保存模式
	 * @param table 表名
	 * @param saveMode 保存模式
	 * @return 逻辑值
	 * */
	public boolean update(String table,SaveMode saveMode)
	{
		DAO dao = getDefaultDAO();
		 return dao.updateRecord(this, table,saveMode);
	}

	/**
	 * 更新数据到来源库、来源表，指定保存模式
	 * @param saveMode 保存模式
	 * @return 逻辑值
	 * */
	public boolean update(SaveMode saveMode)
	{
		DAO dao = getDefaultDAO();
		 return dao.updateRecord(this,saveMode);
	}




	/**
	 * 保存数据到指定库，指定表和保存模式<br>
	 * 在确定场景下，建议使用insert或update以获得更高性能
	 *
	 * @param dao dao
	 * @param table 数据表
	 * @param saveMode 保存模式
	 * @return 更新是否成功
	 * */
	public boolean save(DAO dao,String table,SaveMode saveMode)
	{
		 return  dao.saveRecord(this, table, saveMode);
	}

	/**
	 * 保存数据到来源库，指定表和保存模式<br>
	 * 在确定场景下，建议使用insert或update以获得更高性能
	 * @param table 数据表
	 * @param saveMode 保存模式
	 * @return 更新是否成功
	 * */
	public boolean save(String table,SaveMode saveMode)
	{
		 DAO dao = getDefaultDAO();
		 return dao.saveRecord(this, table,saveMode);
	}

	/**
	 * 保存数据到来源库、来源表，指定保存模式<br>
	 * 在确定场景下，建议使用insert或update以获得更高性能
	 * @param saveMode 保存模式
	 * @return 更新是否成功
	 * */
	public boolean save(SaveMode saveMode)
	{
		DAO dao = getDefaultDAO();

		 return dao.saveRecord(this,saveMode);
	}

	/**
	 * 获得默认DAO，如果有来源DAO则返回来源DAO，如果没有则获取主DAO(有Primary标记的DAO)
	 * */
	private DAO getDefaultDAO() {
		return DAO.getInstance(this.getOwnerSet());
	}

}
