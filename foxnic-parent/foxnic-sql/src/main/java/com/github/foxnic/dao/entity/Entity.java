package com.github.foxnic.dao.entity;

import com.github.foxnic.api.model.CompositeParameter;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Entity implements Serializable , Cloneable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 被修改的属性清单
	 * */
	@ApiModelProperty(hidden = true)
	private transient Set<String> $$dirties=new HashSet<>();

	/**
	 * 被设置过值的属性清单
	 * */
	@ApiModelProperty(hidden = true)
	private transient Set<String> $$besets=new HashSet<>();

	private void init$$() {
		if($$dirties==null) $$dirties=new HashSet<>();
		if($$besets==null) $$besets=new HashSet<>();
	}

	/**
	 * @param field 字段名
	 * @param oldValue 旧值
	 * @param newValue 新值
	 * */
	protected final void change(String field,Object oldValue,Object newValue) {
		init$$();
		boolean isModified=false;
		if(oldValue==null && newValue==null) {
			isModified=false;
		} else if(oldValue==null && newValue!=null) {
			isModified=true;
		} else if(oldValue!=null && newValue==null) {
			isModified=true;
		} else {
			isModified=!oldValue.equals(newValue);
		}

		//设置是否被修改
		if(isModified) {
			$$dirties.add(field);
		}
		//是否被设置过
		$$besets.add(field);

	}


	/**
	 * 标记字段为脏字段
	 * */
	public final void flagDirty(String... propertyName) {
		init$$();
		for (String pn : propertyName) {
			$$dirties.add(pn);
		}
	}


	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	public final boolean hasBeSetProperties() {
		init$$();
		return !$$besets.isEmpty();
	}

	/**
	 * 判断属性是否有被设置过(无论值变化与否)
	 * */
	public final boolean isBeSetProperty(String propertyName) {
		init$$();
		return $$besets.contains(propertyName);
	}

	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	public final boolean hasDirtyProperties() {
		init$$();
		return !$$dirties.isEmpty();
	}

	/**
	 * 判断属性是否有被被修改过，且值被改变
	 * */
	public final boolean isDirtyProperty(String propertyName) {
		init$$();
		return $$dirties.contains(propertyName);
	}

	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	@ApiModelProperty(hidden = true)
	public  final Set<String> besetProperties() {
		init$$();
		return Collections.unmodifiableSet($$besets);
	}

	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	@ApiModelProperty(hidden = true)
	public final Set<String> dirtyProperties() {
		init$$();
		return Collections.unmodifiableSet($$dirties);
	}
	/**
	 * 重置修改状态，标记所有字段为未修改、未被设置过值的状态
	 * */
	public final void clearModifies() {
		init$$();
		$$besets.clear();
		$$dirties.clear();
	};

	public <T> T toPojo(Class<T> pojoType) {
		return null;
	}

	public <T extends Entity> T toPO(Class<T> poType) {
		return null;
	}

//	/**
//	 * 被设置过值的属性清单
//	 * */
//	@ApiModelProperty(hidden = true)
//	private transient Entity $owner = null;
//
//	/**
//	 * 获得所有者对象，在 join 装配时自动设置
//	 * */
//	@Transient
//	@ApiModelProperty(hidden = true)
//	public final Entity $owner() {
//		return $owner;
//	}

//	/**
//	 * 查找上级所有者
//	 * */
//	@Transient
//	@ApiModelProperty(hidden = true)
//	public final <T extends Entity> T findParentOwner(Class<T> ownerType) {
//		Entity ow=this.$owner;
//		while (ow!=null) {
//			if(ownerType.isAssignableFrom(ow.getClass())) {
//				break;
//			}
//			ow=ow.$owner();
//		}
//		return (T) ow;
//	}

	@Transient
	@ApiModelProperty(hidden = true)
	public CompositeParameter getCompositeParameter() {
		return null;
	}

	public void setCompositeParameter(CompositeParameter compositeParameter) {

	}

	/**
	 * 克隆当前对象
	 */
	public Entity clone() {
		throw new RuntimeException("您需要重新生成 "+this.getClass().getName()+" 代码，以便在子类中实现该方法");
	}

	/**
	 * 复制当前对象
	 * @param all 是否复制全部属性，当 false 时，仅复制来自数据表的属性
	 */
	public Entity duplicate(boolean all) {
		throw new RuntimeException("您需要重新生成  "+this.getClass().getName()+" 代码，以便在子类中实现该方法");
	}

	/**
	 * 克隆当前对象
	 */
	public Entity clone(boolean deep) {
		throw new RuntimeException("您需要重新生成 "+this.getClass().getName()+" 代码，以便在子类中实现该方法");
	}



}
