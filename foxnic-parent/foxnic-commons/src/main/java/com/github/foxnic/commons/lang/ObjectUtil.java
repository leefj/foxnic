package com.github.foxnic.commons.lang;

import java.io.*;

public class ObjectUtil {


	/**
	 * 判断两个对象是否相等
	 *
	 * @param v1 对象1
	 * @param v2 对象2
	 * @param nullEqual 两个值都为null时是否相等
	 * @return 逻辑值
	 */
	public static boolean equals(Object v1,Object v2,boolean nullsEqual)
	{
		if(v1==null && v2==null) return nullsEqual;
		else if(v1!=null && v2==null) return false;
		else if(v1==null && v2!=null) return false;
		else {
			return v1.equals(v2);
		}
	}

	/**
	 * 判断两个对象是否相等，如果两个值都为 null 判为相等
	 *
	 * @param v1 对象1
	 * @param v2 对象2
	 * @return 逻辑值
	 */
	public static boolean equals(Object v1,Object v2)
	{
		return equals(v1, v2,true);
	}

	/**
	 * 计算一个对象所占字节数
	 * @param obj 对象，该对象必须继承Serializable接口即可序列化
	 * @return
	 * @throws IOException
	 */
	public static int sizeOf(final Object obj)  {
		if (obj == null) {
			return 0;
		}
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
			ObjectOutputStream out = new ObjectOutputStream(buf);
			out.writeObject(obj);
			out.flush();
			buf.close();
			return buf.size();
		} catch (Exception e) {
			return -1;
		}

	}


	/**
	 * 赋值对象，返回对象的引用，如果参数o为符合对象，则内部每一个对象必须可序列化
	 * @param o 对象，该对象必须继承Serializable接口即可序列化
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object copy(final Object o) throws IOException,
			ClassNotFoundException {
		if (o == null) {
			return null;
		}
		ByteArrayOutputStream outbuf = new ByteArrayOutputStream(4096);
		ObjectOutput out = new ObjectOutputStream(outbuf);
		out.writeObject(o);
		out.flush();
		outbuf.close();
		ByteArrayInputStream inbuf = new ByteArrayInputStream(outbuf.toByteArray());
		ObjectInput in = new ObjectInputStream(inbuf);
		return in.readObject();
	}


}
