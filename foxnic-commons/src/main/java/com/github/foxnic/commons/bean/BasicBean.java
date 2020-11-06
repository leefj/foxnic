package com.github.foxnic.commons.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.foxnic.commons.log.Logger;

public abstract class BasicBean implements Cloneable, Serializable {
 
	private static final long serialVersionUID = 5115181874920873337L;
  
	/**
	 * @param deep  if true, deep clone object
	 * */
	public BasicBean clone(boolean deep) {
		if(deep) {
			try {
				return (BasicBean) super.clone();
			} catch (CloneNotSupportedException e) {
				 Logger.error("normal clone error",e);
				 return null;
			}
		} else {
			return deepClone();
		}
	}
	
	protected BasicBean deepClone() {
		try {
			
			/* 写入当前对象的二进制流 */
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);

			/* 读出二进制流产生的新对象 */
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (BasicBean) ois.readObject();
			
		} catch (Exception e) {
			Logger.error("deep clone error",e);
			return null;
		}
	}
	
	
	 
	public JSONObject toJSONObject() {
		return JSONObject.parseObject(JSON.toJSONString(this));
	}
	
	@Override
	public String toString() {
		 return JSON.toJSONString(this,SerializerFeature.DisableCircularReferenceDetect);
	}

}
