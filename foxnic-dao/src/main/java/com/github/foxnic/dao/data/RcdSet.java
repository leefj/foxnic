package com.github.foxnic.dao.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.FilterOperator;
import com.github.foxnic.commons.bean.PropertyComparator;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.sql.data.ExprRcdSet;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.exception.NoFieldException;
import com.github.foxnic.sql.meta.DBField;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

// TODO: Auto-generated Javadoc
/**
 * 记录集.
 *
 * @author fangjieli
 */
public class RcdSet extends AbstractSet implements ExprRcdSet,Iterable<Rcd>, Serializable {

	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3316786788799093894L;

	/** 记录集合， 大多数情况下不存在高并发问题，ArrayList已经足够。 如有高并发情况，可在考虑性能的情况下，继续对RcdSet进行升级. */
	private ArrayList<Rcd> records = new ArrayList<Rcd>();
	
	/** The is structure locked. */
	private boolean isStructureLocked=false;
	
	 
	/**
	 * Checks if is structure locked.
	 *
	 * @return true, if is structure locked
	 */
	public boolean isStructureLocked() {
		return isStructureLocked;
	}



	/**
	 * Instantiates a new rcd set.
	 */
	public RcdSet()
	{
		this.isStructureLocked=false;
	}
	
	 
	/**
	 * Instantiates a new rcd set.
	 *
	 * @param isStructureLocked the is structure locked
	 */
	public RcdSet(boolean isStructureLocked)
	{
		this.isStructureLocked=isStructureLocked;
	}
	
	 

	/**
	 * Size.
	 *
	 * @return 尺寸
	 */
	@Override
	public int size() {
		return records.size();
	}

	/**
	 * 增加一个记录.
	 *
	 * @param r 记录
	 */
	public void add(Rcd r) {
		this.records.add(r);
	}

	/**
	 * 移除一个记录.
	 *
	 * @param index 位置
	 */
	public void remove(int index) {
		this.records.remove(index);
	}

	/**
	 * 返回指定位置的记录.
	 *
	 * @param i 位置
	 * @return 记录
	 */
	public Rcd getRcd(int i) {
		return records.get(i);
	}

	/** The meta data. */
	protected QueryMetaData metaData = null;

	/**
	 * Gets the raw meta data.
	 *
	 * @return the raw meta data
	 */
	@Override
	protected QueryMetaData getRawMetaData() {
		return metaData;
	}

	/**
	 * Sets the raw meta data.
	 *
	 * @param meta the new raw meta data
	 */
	@Override
	protected void setRawMetaData(QueryMetaData meta) {
		this.metaData = meta;
	}

	/**
	 * 转换为JSONArray，元素为JSONObject.
	 *
	 * @return JSONArray
	 */
	public JSONArray toJSONArrayWithJSONObject() {
		JSONArray arr = new JSONArray();
		for (Rcd r : this) {
			arr.add(r.toJSONObject());
		}
		return arr;
	}

	/**
	 * 转换为JsonArray，内部元素是JsonObject.
	 *
	 * @param includeFields 需要包含的字段，当值为 null 时，默认全部字段
	 * @return JSONArray
	 */
	public JSONArray toJSONArrayWithJSONObject(String... includeFields) {
		return toJSONArrayWithJSONObject(Arrays.asList(includeFields), null);
	}

	/**
	 * 转换为JSONArray，内部元素是JSONObject.
	 *
	 * @param includeFields 需要包含的字段，当值为 null 时，默认全部字段
	 * @param excludeFields 需要排除的字段，当值为 null 时，默认无字段
	 * @return JSONArray
	 */
	public JSONArray toJSONArrayWithJSONObject(List<String> includeFields, List<String> excludeFields) {
		JSONArray arr = new JSONArray();
		for (Rcd r : this) {
			arr.add(r.toJSONObject(includeFields, excludeFields));
		}
		return arr;
	}

	/**
	 * 转换为JSONArray，内部元素是JSONArray；结构与数据分开，可减少数据传输量.
	 *
	 * @return   JSONArray
	 */
	public JSONArray toJSONArrayWithJSONArray() {
		JSONArray arr = new JSONArray();
		for (Rcd r : this) {
			arr.add(r.toJSONArray());
		}
		return arr;
	}

	/**
	 * 转换为JSONObject，内部元素为JSONArray,并以keyfield的指定列作为JSON键.
	 *
	 * @param keyfield 作为key的列序号
	 * @return JSONObject
	 */
	public JSONObject toJSONObjectWithJSONArray(int keyfield) {
		JSONObject json = new JSONObject();
		for (Rcd r : this) {
			json.put(r.getString(keyfield), r.toJSONArray());
		}
		return json;
	}

	/**
	 * 转换为JSONObject，内部元素是JSONArray,并以keyfield的指定列作为JSON键.
	 *
	 * @param keyfield 作为key的列名
	 * @return JSONObject
	 */
	public JSONObject toJSONObjectWithJSONArray(String keyfield) {
		int i=this.metaData.name2index(keyfield);
		if(i==-1) {
			throw new NoFieldException(keyfield);
		}
		return toJSONObjectWithJSONArray(i);
	}

	/**
	 * 转换为JSONObject，内部元素是JSONObject,并以keyfield的指定列作为JSON键.
	 *
	 * @param keyfield 作为key的列序号
	 * @return JSONObject
	 */
	public JSONObject toJSONObjectWithJSONObject(int keyfield) {
		JSONObject json = new JSONObject();
		for (Rcd r : this) {
			json.put(r.getString(keyfield), r.toJSONObject());
		}
		return json;
	}

	/**
	 * 转换为JSONObject，内部元素是JSONObject,并以keyfield的指定列作为JSON键.
	 *
	 * @param keyfield 作为key的列名
	 * @return JSONObject
	 */
	public JSONObject toJSONObjectWithJSONObject(String keyfield) {
		int i=this.metaData.name2index(keyfield);
		if(i==-1) {
			throw new NoFieldException(keyfield);
		}
		return toJSONObjectWithJSONObject(i);
	}

	/**
	 * 通过 for(Record r:set){} 循环遍历.
	 *
	 * @return the iterator
	 */
	@Override
	public Iterator<Rcd> iterator() {
		return records.iterator();
	}
 
	/**
	 * 转换为POJO实体列表.
	 *
	 * @param <T> POJO实体集类型
	 * @param pojoType 实体类型
	 * @return 实体清单
	 */
	public <T> List<T> toEntityList(Class<T> pojoType) {
		List<T> list = new ArrayList<T>(this.size());
		for (Rcd r : this) {
			list.add(r.toEntity(pojoType));
		}
		return list;
	}

	/**
	 * 转换为POJO实体列表(分页).
	 *
	 * @param <T> POJO实体集类型
	 * @param pojoType 实体类型
	 * @return 实体清单
	 */
	public <T> PagedList<T> toPagedEntityList(Class<T> pojoType) {
		List<T> list=this.toEntityList(pojoType);
		return new PagedList<T>(list,this.getMetaData(),this.getPageSize(),this.getPageIndex(),this.getPageCount(),this.getTotalRowCount());
	}


	/**
	 * 使用新的记录集构建一个RcdSet.
	 *
	 * @param rcds the rcds
	 * @return the rcd set
	 */
	private RcdSet subset(ArrayList<Rcd> rcds) {
		return subset(rcds, false);
	}

	/**
	 * 克隆自己，包括结构和数据.
	 *
	 * @return 克隆出来的RcdSet
	 */
	public RcdSet clone() {
		return subset(this.records, true);
	}

	/**
	 * 使用新的记录集构建一个RcdSet.
	 *
	 * @param rcds the rcds
	 * @param withData 是否连同Data一起克隆
	 * @return the rcd set
	 */
	private RcdSet subset(ArrayList<Rcd> rcds, boolean withData) {
		RcdSet rs = new RcdSet();
		rs.metaData = this.metaData.clone();
		rs.metaData.clearSQLInfo();
		rs.metaData.clearTimeInfo();
		if (withData) {
			for (int i = 0; i < rcds.size(); i++) {
				if (rcds.get(i) != null) {
					rcds.set(i, rcds.get(i).clone());
				}
			}
		}
		rs.records = rcds;
		return rs;
	}

	/**
	 * 获得字段清单.
	 *
	 * @return 字段清单
	 */
	public List<String> getFields() {
		return  this.metaData.getFields();
	}

	/**
	 * 取子集.
	 *
	 * @param fromIndex 起始位置
	 * @param toIndex   结束位置
	 * @param cloneData 是否克隆数据(Rcd)
	 * @return 子集
	 */
	public RcdSet subset(int fromIndex, int toIndex, boolean cloneData) {
		ArrayList<Rcd> list = (ArrayList<Rcd>) this.records.subList(fromIndex, toIndex);
		return subset(list, cloneData);
	}
	
	/**
	 * 判断列是否存在
	 * 
	 * @param name 列名
	 * */
	public boolean hasColumn(String name) {
		return this.getMetaData().name2index(name)>=0;
	}

	/**
	 * 删除列.
	 *
	 * @param name 列名
	 */
	public void removeColumn(String name) {
		
		checkStructureLocking();
		
		int i = this.metaData.name2index(name);
		if(i==-1) {
			throw new NoFieldException(name);
		}
		this.metaData.deleteColumn(i);
		for (Rcd r : records) {
			r.removeColumn(i);
		}
	}

	/**
	 * 从现有记录集 返回一个 distinct 处理后的新记录集.
	 *
	 * @param fields 字段，以这些字段值作为distinct的键
	 * @return distinct后的子集
	 */
	public RcdSet distinct(String... fields) {
		if (fields.length == 0) {
			return this;
		}

		ArrayList<Rcd> rcds = new ArrayList<Rcd>();
		ArrayList<String> keys = new ArrayList<String>();
		String key = "";
		for (Rcd rcd : records) {
			key = "";
			for (String f : fields) {
				key += rcd.getValue(f) + ",";
			}
			if (keys.contains(key)) {
				continue;
			}
			rcds.add(rcd);
			keys.add(key);
		}

		RcdSet newRs = this.subset(rcds);
		return newRs;

	}
	

 
	/**
	 * 获得字段值清单.
	 *
	 * @param field 字段名称
	 * @return 返回Object类型list
	 */
	public List<Object> getValueList(String field) {
		ArrayList<Object> list = new ArrayList<Object>();
		for (Rcd r : this) {
			list.add(r.getValue(field));
		}
		return list;
	}
	
	/**
	 * 获得字段值清单<br> 示例  List&lt;String&gt; names=rs.getValueList("name",String.class);
	 * @param <T> 指定需要获得的值类型
	 * @param field 字段名称
	 * @param type  值类型
	 * @return 返回Object类型list
	 */
	public <T> List<T> getValueList(String field,Class<T> type) {
		ArrayList<T> list = new ArrayList<T>();
		for (Rcd r : this) {
			list.add(DataParser.parse(type,r.getValue(field)));
		}
		return list;
	}
	
	
	
	
	/**
	 * 获得字段值清单，去重.
	 *
	 * @param field 字段名称
	 * @return 返回Object 类型的 set 集合
	 */
	public Set<Object> getValueSet(String field) {
		HashSet<Object> list = new HashSet<Object>();
		for (Rcd r : this) {
			list.add(r.getValue(field));
		}
		return list;
	}
	
	/**
	 * 获得字段值清单，去重<br> 示例  Set&lt;String&gt; names=rs.getValueSet("name",String.class);
	 * @param <T> 指定需要获得的值类型
	 * @param field 字段名称
	 * @param type  值类型
	 * @return 返回Object 类型的 set 集合
	 */
	public <T> Set<T> getValueSet(String field,Class<T> type) {
		HashSet<T> list = new HashSet<>();
		for (Rcd r : this) {
			list.add(DataParser.parse(type,r.getValue(field)));
		}
		return list;
	}
	
	
	/**
	 * 获得字段值清单，去重.
	 *
	 * @param field 字段名称
	 * @return 返回Object 类型的数组
	 */
	public Object[] getValueArray(String field) {
		Object[] array = new Object[this.size()];
		int i=0;
		for (Rcd r : this) {
			array[i]=r.getValue(field);
			i++;
		}
		return array;
	}
	
	/**
	 * 获得字段值清单，<br> 示例  String[] names=rs.getValueArray("name",String.class);
	 * @param <T> 指定需要获得的值类型
	 * @param field 字段名称
	 * @param type  值类型
	 * @return 返回Object 类型的数组
	 */
	public <T>  T[] getValueArray(String field,Class<T> type) {
		T[] array = ArrayUtil.createArray(type, this.size());
		int i=0;
		for (Rcd r : this) {
			array[i]=DataParser.parse(type,r.getValue(field));
			i++;
		}
		return array;
	}
	

	/**
	 * 获得记录集 List.
	 *
	 * @return  录集 List
	 */
	public List<Rcd> getRcdList() {
		return Collections.unmodifiableList(records);
	}

	 
	/**
	 * 排序.
	 *
	 * @param field 字段
	 * @param ascending 是否升序，否则降序
	 * @param nullslast null值是否排在最后
	 * @return 当前记录集
	 */
	public RcdSet sort(final String field,  boolean ascending, final boolean nullslast) {
		
		int fieldIndex=this.getMetaData().name2index(field);
		if(fieldIndex==-1) {
			throw new NoFieldException(field);
		}
		Object value=null;
		Collections.sort(records, new Comparator<Rcd>() {
			@Override
			public int compare(Rcd o1, Rcd o2) {
				Object v1 = o1.getValue(field);
				Object v2 = o2.getValue(field);
				int i=PropertyComparator.compareValue(v1, v2, true,ascending);
				return i;
			}
		});
		
		//对非 nullslast 的处理
		if(this.size()>1) {
			value=records.get(0).getValue(fieldIndex);
			if(!nullslast && value!=null) {
				Rcd r=null;
				while(true) {
					if(records.isEmpty()) break;
					r=records.get(records.size()-1);
					value=r.getValue(fieldIndex);
					if(value==null)
					{
						records.remove(records.size()-1);
						records.add(r);
					}
				}
			}
		}
		return this;
	}
	
	
	 /**
 	 * 填充排名字段，如字段不存在则增加字段.
 	 *
 	 * @param rankField            用来存储排名序号的字段
 	 * @param valueField            按照这个列的数据进行排名
 	 * @param asc 是否升序
 	 */
    public void fillRankField(String rankField, String valueField, final boolean asc) {
        int rankFieldIndex = this.getMetaData().name2index(rankField);
        int valueFieldIndex = this.getMetaData().name2index(valueField);

        if (rankFieldIndex == -1) {
            this.addColumn(rankField, new Object[this.size()]);
            rankFieldIndex = this.getMetaData().name2index(rankField);
        }

        ArrayList<Rcd> rs = (ArrayList<Rcd>) this.records.clone();

        rs.sort(new Comparator<Rcd>() {

            @Override
            public int compare(Rcd r1, Rcd r2) {
                Double d1 = r1.getDouble(valueFieldIndex);
                Double d2 = r2.getDouble(valueFieldIndex);

                int flag = 0;

                if (d1 == null && d2 == null)
                    flag = 0;
                else if (d1 != null && d2 == null)
                    flag = 1;
                else if (d1 == null && d2 != null)
                    flag = -1;
                else {
                    if (d1 > d2)
                        flag = 1;
                    else if (d1 == d2)
                        flag = 0;
                    else if (d1 < d2)
                        flag = -1;
                }

                if (!asc) {
                    flag = flag * -1;
                }

                return flag;
            }
        });

        int i = 1;
        for (Rcd r : rs) {
            r.setValue(rankFieldIndex, i);
            i++;
        }

    }
	

	/**
	 * Lambda Stream.
	 *
	 * @return Stream of Rcd
	 */
	public Stream<Rcd> stream() {
		return this.records.stream();
	}
	
	/**
	 * 把记录集中的数据转换成Map形式，并完成默认的类型转换.
	 *
	 * @param keyField 键值列
	 * @param valueField 值列
	 * @return Map
	 */
	@SuppressWarnings("rawtypes")
	public Map getValueMap(String keyField,String valueField) {
		return getValueMap(keyField,null,valueField,null);
	}

	/**
	 * 把记录集中的数据转换成Map形式，并完成指定的类型转换.
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @param keyField 键值列
	 * @param keyType 键值类型
	 * @param valueField 值列
	 * @param valueType 值类型
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	public <K,V> Map<K,V> getValueMap(DBField keyField,Class<K> keyType,String valueField,Class<V> valueType) {
		return this.getValueMap(keyField.name(), keyType, valueField, valueType);
	}
	
	/**
	 * 把记录集中的数据转换成Map形式，并完成指定的类型转换.
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @param keyField 键值列
	 * @param keyType 键值类型
	 * @param valueField 值列
	 * @param valueType 值类型
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	public <K,V> Map<K,V> getValueMap(String keyField,Class<K> keyType,String valueField,Class<V> valueType) {

		int keyIndex=this.getMetaData().name2index(keyField);
		int valueIndex=this.getMetaData().name2index(valueField);
		
		if(keyIndex==-1) {
			throw new NoFieldException(keyField);
		}
		if(keyIndex==-1) {
			throw new NoFieldException(valueField);
		}
		
		Object tmp=null;
		K key=null;
		V value=null;
		HashMap<K, V> map = new HashMap<K, V>();
		
		for (Rcd r : this) {
			
			tmp=r.getValue(keyIndex);
			if(keyType!=null) key=DataParser.parse(keyType, tmp);
			else key=(K)tmp;
			
			tmp=r.getValue(valueIndex);
			if(valueType!=null) value=DataParser.parse(valueType, tmp);
			else value=(V)tmp;
			map.put(key,value);
		}
		return map;
	}

	/**
	 * 把记录集转换成Map形式 如果是多个键，则使用他们的值使用下划线进行连接.
	 *
	 * @param field 作为key的列名
	 * @return Map
	 */
	public Map<Object, Rcd> getMappedRcds(String... field) {

		HashMap<Object, Rcd> map = new HashMap<Object, Rcd>(this.size());
		for (Rcd r : this) {
			map.put(makeGroupKey(r, field), r);
		}
		return map;
		
	}
	
	/**
	 * 把记录集转换成Map形式.
	 *
	 * @param <K> key类型
	 * @param field 作为key值的字段
	 * @param keyType key值类型
	 * @return Map
	 */
	public <K> Map<K, Rcd> getMappedRcds(String field,Class<K> keyType) {
		HashMap<K, Rcd> map = new HashMap<K, Rcd>(this.size());
		for (Rcd r : this) {
			map.put(DataParser.parse(keyType,r.getValue(field)), r);
		}
		return map;
	}

	/**
	 * 获得用于分组的key,各个字段间用下划线隔开 如果一个key则使用原始值作为key，如果多个key则使用.
	 *
	 * @param r 记录
	 * @param field 用于key制作的字段
	 * @return key
	 */
	public static Object makeGroupKey(Rcd r, String... field) {
		if (field.length == 0) {
			throw new IllegalArgumentException("至少包含一个字段");
		}

		if (field.length == 1) {
			return r.getValue(field[0]);
		} else {
			String key = "";
			for (int i = 0; i < field.length; i++) {
				key += r.getString(field[i]) + "_";
			}
			if (key.length() > 0) {
				key = key.substring(0, key.length() - 1);
			}
			return key;
		}
	}

	/**
	 * 把记录集转换成JSON形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接.
	 *
	 * @param field key字段
	 * @return JSONObject
	 */
	public JSONObject getGroupedJSON(String... field) {
		JSONObject map = new JSONObject();
		String key = null;
		JSONArray list = null;
		for (Rcd r : this) {
			key = makeGroupKey(r, field) + "";
			list = map.getJSONArray(key);
			if (list == null) {
				list = new JSONArray();
				map.put(key, list);
			}
			list.add(r.toJSONObject());
		}
		return map;
	}

	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接.
	 *
	 * @param field key字段
	 * @return Map
	 */
	public Map<Object, List<Rcd>> getGroupedMap(String... field) {

		HashMap<Object, List<Rcd>> map = new HashMap<Object, List<Rcd>>(5);
		Object key = null;
		List<Rcd> list = null;
		for (Rcd r : this) {
			key = makeGroupKey(r, field);
			list = map.get(key);
			if (list == null) {
				list = new ArrayList<Rcd>();
				map.put(key, list);
			}
			list.add(r);
		}
		return map;
	}
	
	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接.
	 *
	 * @param <K> key类型
	 * @param field key字段
	 * @param keyType 键值类型
	 * @return Map 返回类型为 LinkedHashMap  有顺序的
	 */
	public <K> Map<K, List<Rcd>> getGroupedMap(DBField field,Class<K> keyType) {
		return this.getGroupedMap(field.name(), keyType);
	}
	
	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接.
	 *
	 * @param <K> key类型
	 * @param field key字段
	 * @param keyType 键值类型
	 * @return Map 返回类型为 LinkedHashMap  有顺序的
	 */
	public <K> Map<K, List<Rcd>> getGroupedMap(String field,Class<K> keyType) {

		LinkedHashMap<K, List<Rcd>> map = new LinkedHashMap<K, List<Rcd>>(5);
		K key = null;
		List<Rcd> list = null;
		for (Rcd r : this) {
			key = DataParser.parse(keyType,r.getValue(field));
			list = map.get(key);
			if (list == null) {
				list = new ArrayList<Rcd>();
				map.put(key, list);
			}
			list.add(r);
		}
		return map;
	}
	

	/**
	 * 增加一个实际数据列.
	 *
	 * @param field 列名
	 */
	public void addColumn(String field) {
		
		checkStructureLocking();
		
		this.addColumn(field, new Object[this.size()]);
	}
	
	/**
	 * Check structure locking.
	 */
	private void checkStructureLocking() {
		if(this.isStructureLocked) {
			throw new DBMetaException("已锁定，不与许改变记录集结构");
		}
	}



	/**
	 * 变更字段名，把oldLabel列的列名变更为newLabel.
	 *
	 * @param oldLabel   原字段名
	 * @param newLabel  新字段名
	 */
	public void changeColumnLabel(String oldLabel,String newLabel)
	{
		checkStructureLocking();
		this.getMetaData().changeColumnLabel(oldLabel, newLabel);
	}
	
	/**
	 * 改变该列值的数据类型，数据的Meta信息不变.
	 *
	 * @param field 列名
	 * @param type  将要变更的类型
	 */
	public void changeColumnType(String field,Class<?> type)
	{
		checkStructureLocking();
		int index=this.getMetaData().name2index(field);
		if(index==-1) {
			throw new NoFieldException(field);
		}
		for (Rcd r : records) {
			r.set(index,  DataParser.parse(type,r.getValue(index)));
		}
	}
	
	 

	 

	/**
	 * 增加一个实际数据列.
	 *
	 * @param colName  列名
	 * @param data 数据
	 */
	public void addColumn(String colName, Object[] data) {
 
		checkStructureLocking();
		
		if (this.size() == 0) {
			return;
		}

		for (int i = 0; i < data.length; i++) {
			Rcd r = this.getRcd(i);
			r.addValue(data[i]);
		}

		if (data.length < this.size()) {
			for (int i = data.length; i < this.size(); i++) {
				Rcd r = this.getRcd(i);
				r.setValue(i, null);
			}
		}

		metaData.addCatalogName("");
		metaData.addColumnClassName("");
		metaData.addColumnLabel(colName);
		metaData.addColumnType(0);
		metaData.addColumnTypeName("");
		metaData.addSchemaName("");
		metaData.addTableName("");
		metaData.setMap(colName, metaData.getColumnCount());
		metaData.setColumnCount(metaData.getColumnCount() + 1);

	}

	/**
	 * 获得某个字段为某个值的记录子集.
	 *
	 * @param field 列名
	 * @param value 值，获得与该值相等的子集
	 * @return 子集
	 */
	public RcdSet filter(String field, Object value) {
		return filter(field, value, FilterOperator.EQUALS);
	}
	
	/**
	 * 获得某个字段为某个值的记录子集.
	 *
	 * @param field 列名
	 * @param value 值，获得与该值相等的子集
	 * @param filterOperator  过滤操作符号
	 * @return 子集
	 */
	public RcdSet filter(String field, Object value,FilterOperator filterOperator) {
		
		int index = this.getMetaData().name2index(field);
		
		if(index==-1) {
			throw new NoFieldException(field);
		}
		
		ArrayList<Rcd> list = new ArrayList<Rcd>();
		
		if(value!=null)
		for (Rcd r : this) {
			if(r.getValue(index)!=null) {
				if(!value.getClass().equals(r.getValue(index).getClass())) {
					throw new RuntimeException("类型不一致,列类型:"+r.getValue(index).getClass().getName()+",过滤值类型:"+value.getClass().getName());
				} else {
					break;
				}
			}
		}
		
		
		Object tmp=null;
		for (Rcd r : this) {
			tmp=r.getValue(index);
			if(filterOperator.compare(tmp,value)) {
				list.add(r);
			}
		}

		return this.subset(list, false);
	}

	/**
	 * 填充排名字段，如字段不存在则增加字段, 如
	 * rank("PaiMing","age",true)，就是指定一个PaiMing字段，并把按照age字段排名的结果存入PaiMing中，并且排序方式为升序.
	 *
	 * @param rankField  用来存储排名序号的字段（如果不存在就自动增加字段
	 * @param valueField 按照这个列的数据进行排名
	 * @param ascending 是否升序排名，否则降序
	 */
	public void rank(String rankField, String valueField, final boolean ascending) {
		int rankFieldIndex = this.metaData.name2index(rankField);
		int valueFieldIndex = this.metaData.name2index(valueField);

		if (rankFieldIndex == -1) {
			this.addColumn(rankField);
			rankFieldIndex = this.metaData.name2index(rankField);
		}

		ArrayList<Rcd> rs = (ArrayList<Rcd>) this.records.clone();

		rs.sort(new Comparator<Rcd>() {
			@Override
			public int compare(Rcd r1, Rcd r2) {
				Double d1 = r1.getDouble(valueFieldIndex);
				Double d2 = r2.getDouble(valueFieldIndex);
				int flag = 0;
				if (d1 == null && d2 == null) {
					flag = 0;
				} else if (d1 != null && d2 == null) {
					flag = 1;
				} else if (d1 == null && d2 != null) {
					flag = -1;
				} else {
					if (d1 > d2) {
						flag = 1;
					} else if (d1.equals(d2)) {
						flag = 0;
					} else if (d1 < d2) {
						flag = -1;
					}
				}
				if (!ascending) {
					flag = flag * -1;
				}
				return flag;
			}
		});

		int i = 1;
		for (Rcd r : rs) {
			r.setValue(rankFieldIndex, i);
			i++;
		}
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return this.toJSONArrayWithJSONObject().toJSONString();
	}

	/**
	 * 返回第一个匹配的记录.
	 *
	 * @param field      字段
	 * @param value      值 ，两个null值也视为相等
	 * @return Rcd
	 */
	public Rcd find(String field, String value) {
		return find(field, value, 0);
	}

	/**
	 * 返回第一个匹配的记录,如果指定列的类型和目标值类型不一致，则转换类型后再行比较.
	 *
	 * @param field 字段
	 * @param value 值 ，两个null值也视为相等
	 * @param startIndex the start index
	 * @return  返回匹配的第一个记录
	 */
	@SuppressWarnings("unchecked")
	public Rcd find(String field, Object value,int startIndex) {
		int i=this.getMetaData().name2index(field);
		if(i==-1) {
			throw new NoFieldException(field);
		}
		Object dv=null;
		Class type=value==null?null:value.getClass();
		for (int j = startIndex; j < records.size(); j++) {
			Rcd r = records.get(j);
			dv=r.getValue(i);
			if(dv==null &&  value==null) return r;
			if(type!=null) dv=DataParser.parse(type, dv);
			if(value!=null && value.equals(dv)) return r;
		}
		return null;
	}
	
	

}
