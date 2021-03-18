package com.github.foxnic.commons.io;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class FileUtil {

	/**
	 * 获得相对于指定基础路径的文件
	 */
	public static File resolveByPath(String basicDirPath, String... part) {
		part = ArrayUtil.unshift(part, basicDirPath);
		return  new File(StringUtil.joinPath(part));
	}

	/**
	 * 获得相对于指定基础路径的文件
	 */
	public static File resolveByPath(File basicDir, String... part) {
		return resolveByPath(basicDir.getAbsolutePath(), part);
	}

	/**
	 * 获得相对于指定类所在目录的文件
	 * 
	 * @param cls 类
	 * @return 类所在的路径
	 */
	public static File resolveByClass(Class cls, String... part) {
		File dir = resolveByClass(cls);
		return resolveByPath(dir.getParentFile(), part);
	}

	/**
	 * 获得相对于主调类所在目录的文件
	 * 
	 * @param cls 类
	 * @return 类所在的路径
	 */
	public static File resolveByInvoke(String... part) {
		Class cls = ReflectUtil.forName((new Throwable()).getStackTrace()[1].getClassName(), true);
		return resolveByClass(cls, part);
	}

	/**
	 * 获得主调类所在的路径
	 * 
	 * @param cls 类
	 * @return 类所在的路径
	 */
	public static File resolveByInvoke() {
		Class cls = ReflectUtil.forName((new Throwable()).getStackTrace()[1].getClassName(), true);
		return resolveByClass(cls);
	}

	/**
	 * 获得类所在的路径
	 * 
	 * @param cls 类
	 * @return 类所在的路径
	 */
	public static File resolveByClass(Class cls) {

		String strURL = "";
		try {
			String strClassName = cls.getName();
			String strPackageName = "";
			if (cls.getPackage() != null) {
				strPackageName = cls.getPackage().getName();
			}

			String strClassFileName = "";
			if (!"".equals(strPackageName)) {
				strClassFileName = strClassName.substring(strPackageName.length() + 1, strClassName.length());
			} else {
				strClassFileName = strClassName;
			}

			URL url = null;
			url = cls.getResource(strClassFileName + ".class");
			strURL = url.toString();
			
			String _strURL=StringUtil.removeFirst(strURL, "file:/");
			if(!OSType.isWindows()) {
				_strURL="/"+_strURL;
			}
			File f=new File(_strURL);
			if(f.exists() && f.isFile()) {
				return f;
			}

			

			try {
				strURL = java.net.URLDecoder.decode(strURL, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Logger.error("path resolve error", e);
			}

			return new File(strURL);
		} catch (Exception e) {
			Logger.error("path resolve error", e);
			return null;
		}

	}

	public static void writeText(File f, CharSequence text, String encoding) {
		try {
			FileUtils.write(f, text, encoding);
		} catch (IOException e) {
			Logger.error("write file error, file exists=" + f.exists() + " , " + f.getAbsolutePath());
		}
	}

	public static void writeText(File f, CharSequence text) {
		writeText(f, text, "UTF-8");
	}

	public static String readText(File f, String encoding) {
		try {
			return FileUtils.readFileToString(f, encoding);
		} catch (IOException e) {
			Logger.error("read file error, file exists=" + f.exists() + " , " + f.getAbsolutePath());
			return null;
		}
	}
	
	/**
	 * 获得  file 相对于 baseDir 的路径
	 * @param baseDir  基础路径
	 * @param file 完整路径
	 * @return 相对路径
	 * */
	public static String getRelativePath(File baseDir,File file)
	{
		String base=baseDir.getAbsolutePath();
		String full=file.getAbsolutePath();
		if(!full.startsWith(base)) {
			throw new IllegalArgumentException(full + " is not sub of "+base);
		}
		return full.substring(base.length());
	}

	public static String readText(File f) {
		return readText(f, "UTF-8");
	}

	public static JSONObject readJSONobject(File f) {
		return JSONObject.parseObject(readText(f));
	}

	public static JSONObject readJSONobject(File f, String encoding) {
		return JSONObject.parseObject(readText(f, encoding));
	}

	public static String changeExtName(String rel, String newExtName) {
		int i=rel.lastIndexOf(".");
		newExtName=StringUtil.removeFirst(newExtName, ".");
		if(i!=-1) {
			rel=rel.substring(0,i);
			return rel+"."+newExtName;
		} else {
			return rel+"."+newExtName;
		}
	}

	public static FileInputStream getInputStream(File file) throws Exception {
		return new FileInputStream(file);
	}
	
	public static FileInputStream getInputStream(String file) throws Exception {
		return new FileInputStream(new File(file));
	}

	public static boolean isInPath(File dir, String... dirName) {
		int i=0;
		for (String dn : dirName) {
			File p=dir;
			while(p!=null) {
				if(p.getName().equals(dn)) {
					i++;
					break;
				}
				p=p.getParentFile();
			}
		}
 
		return dirName.length==i;
	}
	
	
	/**
	 * 创建临时文件到默认目录，临时文件并不会自行消失，类似缓存文件
	 * @param prefix 文件前缀
	 * @param suffix  文件后缀，可包含扩展名
	 * @return EasyFile
	 * @throws Exception  异常
	 * */
	public static File createTempFile(String prefix,String suffix) {
		try {
			return File.createTempFile(prefix, suffix);
		} catch (IOException e) {
			 throw new RuntimeException(e);
		}
	}
	
	/**
	 * 创建临时文件到指定目录，临时文件并不会自行消失，类似缓存文件
	 * @param prefix 文件前缀
	 * @param suffix  文件后缀，可包含扩展名
	 * @param dir 指定目录
	 * @return File
	 * @throws Exception 异常
	 * */
	public static File createTempFile(String prefix,String suffix,File dir) {
		try {
			return File.createTempFile(prefix, suffix,dir);
		} catch (IOException e) {
			 throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 删除文件
	 * @return  删除是否成功
	 * */
	public static boolean delete(File file) {
		return file.delete();
	}
	
	/**
	 * 连同所有文件或文件夹
	 * @param contents 是否文件夹中的内容一起删除
	 * @return 是否删除成功
	 * */
	public static boolean delete(File dir,boolean contents) {
		if (contents) {
			try {
				if (dir.exists()) {
					FileUtils.deleteDirectory(dir);
				}
				return true;
			} catch (IOException e) {
				Logger.error(e);
				return false;
			}
		} else {
			return dir.delete();
		}
	}
}
