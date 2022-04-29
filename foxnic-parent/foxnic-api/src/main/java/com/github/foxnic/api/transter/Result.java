package com.github.foxnic.api.transter;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;

public class Result<T> implements Serializable {



    public static class Extra {

		/**
		 * 不提示信息
		 */
		public static final String MESSAGE_LEVEL_NONE="none";
		/**
		 * 简单快速的提示信息
		 */
		public static final String MESSAGE_LEVEL_NOTIFY="notify";
		/**
		 * 允许用户能仔细阅读
		 * */
		public static final String MESSAGE_LEVEL_READ="read";

		/**
		 * 允许用户点击确认
		 * */
		public static final String MESSAGE_LEVEL_CONFIRM="confirm";

		@ApiModelProperty(notes = "请求响应时间戳",example = "1614595847386")
		private Long time;
		@ApiModelProperty(notes = "日志跟踪ID,在headder中传入",example = "418842786398208000")
		private String tid;
		@ApiModelProperty(notes = "执行耗时(毫秒)",example = "56")
		private Long cost;
		@ApiModelProperty(notes = "data域的数据类型",example = "java.util.List")
		private String dataType;
		@ApiModelProperty(notes = "当data域为集合类型时的元素类型",example = "java.lang.String")
		private String componentType;
		@ApiModelProperty(notes = "控制器方法",example = "com.xyz.UserController.listUser")
		private String method;
		@ApiModelProperty(notes = "异常信息",example = "")
		private String exception;
		@ApiModelProperty(notes = "消息提示的级别",example = "notify")
		private String messageLevel = null;



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
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		public String getException() {
			return exception;
		}
		public void setException(Throwable exception) {
			this.exception = toString(exception);
		}
		public void setException(String exception) {
			this.exception = exception;
		}

		public String getMessageLevel() {
			return messageLevel;
		}

		/**
		 * 设置前端信息提示的级别为 notify , 尽量快速的提示并隐藏
		 * */
		public void  messageLevel4Notify() {
			this.messageLevel=MESSAGE_LEVEL_NOTIFY;
		}

		/**
		 * 设置前端信息提示的级别为 read , 显示的时间适合阅读
		 * */
		public void  messageLevel4Read() {
			this.messageLevel=MESSAGE_LEVEL_READ;
		}

		public void setMessageLevel(String messageLevel) {
			this.messageLevel = messageLevel;
		}

		/**
		 * 设置前端信息提示的级别为 confirm , 需要用户点击确认
		 * */
		public void  messageLevel4Confirm() {
			this.messageLevel=MESSAGE_LEVEL_CONFIRM;
		}

		public static String toString(Throwable e) {
			if(e==null) return null;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String content = sw.toString();
			try {
				sw.close();
				pw.close();
			} catch (IOException e1) {
			}
			return content;
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

	@ApiModelProperty(required = true,notes = "引用的数据，数据字典，枚举等",example = "")
	private Map<String,Object> refer;

	public Map<String,Object> getRefer() {
		return refer;
	}

	public void refer(String key,Object refer) {
		if(this.refer==null) this.refer=new HashMap<>();
		this.refer.put(key,refer);
	}

	@ApiModelProperty(required = true,notes = "错误详情",example = "[]")
	private List<Result> errors=null;

	@ApiModelProperty(required = true,notes = "解决方案",example = "[]")
	private Set<String> solutions=null;

	public Set<String> getSolutions() {
		return solutions;
	}

	/**
	 * 加入错误处理的解决方案
	 * **/
	public Result addSolution(String solution) {
		if(this.solutions==null) {
			this.solutions = new HashSet<>();
		}
		this.solutions.add(solution);
		return this;
	}



	public Result<T> data(T data) {
		this.data = data;
		if(this.data!=null) {
			this.extra().dataType=this.data.getClass().getName();
			//识别元素类型
			if(this.data.getClass().isArray()) {
				this.extra().componentType=this.data.getClass().getComponentType().getName();
			} else if (this.data instanceof Collection) {
				Collection coll=(Collection)this.data;
				if(!coll.isEmpty()) {
					Object el=null;
					while(el==null) {
						el=coll.iterator().next();
					}
					if(el!=null) {
						this.extra().componentType=el.getClass().getName();
					}
				}
			}
		} else {
			this.extra().dataType=null;
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

	@ApiModelProperty(required = true,notes = "错误主体",example = "姓名")
	private String subject = null;

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

	public Result<T> subject(String subject) {
		this.subject=subject;
		return this;
	}

	public boolean failure() {
		return !success;
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
		// 如果没有手工修改过
		if(this.extra().getMessageLevel()==null) {
			if (this.success) {
				this.extra().messageLevel4Notify();
			} else {
				this.extra().messageLevel4Read();
			}
		}
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
	private Extra extra=null;

	public Extra extra() {
		if(extra==null) {
			extra=new Extra();
		}
		return extra;
	}

	/**
	 * 为了兼容 Knife4j 加的方法，等同于 extra 方法
	 * */
	public Extra getExtra() {
		return extra;
	}

	public List<Result> getErrors() {
		return errors;
	}

	public boolean hasSubError() {
		return this.errors!=null &&!this.errors.isEmpty();
	}

	public Result<T> addErrors(List<Result> errors) {
		if(this.errors==null) {
			this.errors=new ArrayList<>();
		}
		this.errors.addAll(errors);
		this.success(false);
		return this;
	}

	public Result<T> addError(Result error) {
		if(this.errors==null) {
			this.errors=new ArrayList<>();
		}
		this.errors.add(error);
		this.success(false);
		return this;
	}

	public Result<T> addError(String message) {
		Result error=new Result();
		error.success(false).message(message);
		return this.addError(error);
	}

	public Result<T> addError(String message,Object data) {
		Result error=new Result();
		error.success(false).message(message).data(data);
		return this.addError(error);
	}



	public String getMessage() {
		return message;
	}

	public String getSubject() {
		return subject;
	}

	/**
	 * 设置前端信息提示的级别为 notify , 尽量快速的提示并隐藏
	 * */
	public Result<T>  messageLevel4Notify() {
		this.extra().messageLevel4Notify();
		return this;
	}

	/**
	 * 设置前端信息提示的级别为 read , 显示的时间适合阅读
	 * */
	public Result<T>  messageLevel4Read() {
		this.extra().messageLevel4Read();
		return this;
	}

	/**
	 * 设置前端信息提示的级别为 confirm , 需要用户点击确认
	 * */
	public Result<T>  messageLevel4Confirm() {
		this.extra().messageLevel4Confirm();
		return this;
	}





}
