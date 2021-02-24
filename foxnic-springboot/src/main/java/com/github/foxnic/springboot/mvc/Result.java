package com.github.foxnic.springboot.mvc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import io.swagger.annotations.ApiModelProperty;
 
public class Result<T> implements Serializable {

	public static class Extra {
		private Long time;
		private String tid;
		private Long cost;
		private String dataType;
		private String componentType;
		
		public Long getTime() {
			return time;
		}
		public void setTime(Long time) {
			this.time = time;
		}
		public String getTid() {
			return tid;
		}
		public void setTid(String tid) {
			this.tid = tid;
		}
		public Long getCost() {
			return cost;
		}
		public void setCost(Long cost) {
			this.cost = cost;
		}
		public String getDataType() {
			return dataType;
		}
		public void setDataType(String dataType) {
			this.dataType = dataType;
		}
		public String getComponentType() {
			return componentType;
		}
		public void setComponentType(String componentType) {
			this.componentType = componentType;
		}
	}
	
	private static final long serialVersionUID = -1902733018654069374L;

	/**
	 * 默认为成功
	 * */
	public Result() {
		this(true);
	}
	
	public Result(boolean success) {
		this.success(success);
	}
	
	@ApiModelProperty(required = true,notes = "数据",example = "{\"id\":1,\"name\":\"blues\"}")
	private T data;
	
	 

	public Result<T> data(T data) {
		this.data = data;
		if(this.data!=null) {
			this.extra.dataType=this.data.getClass().getName();
			//识别元素类型
			if(this.data.getClass().isArray()) {
				this.extra.componentType=this.data.getClass().getComponentType().getName();
			} else if (this.data instanceof Collection) {
				Collection coll=(Collection)this.data;
				if(!coll.isEmpty()) {
					Object el=null;
					while(el==null) {
						el=coll.iterator().next();
					}
					if(el!=null) {
						this.extra.componentType=el.getClass().getName();
					}
				}
			}
		} else { 
			this.extra.dataType=null;
		}
		
		
		
		return this;
	}

	public T data() {
		return data;
	}
	
	/**
	 * 为了兼容 Knife4j 加的方法，等同于 data 方法
	 * */
	public T getData() {
		return data();
	}
	
	@ApiModelProperty(required = true,notes = "是否处理成功",example = "true")
	private boolean success = true;
	
	@ApiModelProperty(required = true,notes = "结果码",example = "01")
	private String code;
	
	public String code() {
		return code;
	}
	
	/**
	 * 为了兼容 Knife4j 加的方法，等同于 code 方法
	 * */
	public String getCode() {
		return code();
	}

	public Result<T> code(String code) {
		this.code = code;
		return this;
	}

	@ApiModelProperty(notes = "提示信息",example = "操作成功")
	private String message;

	public boolean success() {
		return success;
	}
	
	/**
	 * 为了兼容 Knife4j 加的方法，等同于 success 方法
	 * */
	public boolean isSuccess() {
		return success();
	}
	
	/**
	 * 把 source 作为一个错误的结果进行复制 
	 * */
	@SuppressWarnings("rawtypes")
	public Result<T> copyAsError(Result source) {
		return  this.success(false).code(source.code()).message(source.message());
	 }

	public Result<T> success(boolean success) {
		this.success = success;
		return this;
	}
 
	public String message() {
		return message;
	}

	public Result<T> message(String message) {
		this.message = message;
		return this;
	}

	/**
	 * 获得data属性中的实体对象，并转换为指定的实体类型
	 * */
	public <E> E getData(Class<E> type) {
		if(this.data == null) return null;
		E e=JSON.parseObject(JSON.toJSONString(this.data), type);
		return e;
	}
	
	/**
	 * 如果data是一个map，获得data属性中的数据对象，并转换为指定的实体类型
	 * */
	public <E> E getDataByKey(Object key, Class<E> type) {
		if(this.data == null) return null;
		if(!(this.data instanceof Map)) {
			throw new IllegalArgumentException("data is not type of Map");
		}
		Map map=(Map)this.data;
		Object keyData=map.get(key);
		E e=JSON.parseObject(JSON.toJSONString(keyData), type);
		return e;
	}
 
	@ApiModelProperty(notes = "扩展信息",example = "")
	private Extra extra=new Extra();
	
	public Extra extra() {
		return extra;
	}
	
	/**
	 * 为了兼容 Knife4j 加的方法，等同于 extra 方法
	 * */
	public Extra getExtra() {
		return extra;
	}
	 

	
}
