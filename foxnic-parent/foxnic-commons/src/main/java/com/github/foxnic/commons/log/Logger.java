package com.github.foxnic.commons.log;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NOPLogger;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;

/**
 * 日志工具,slf4j调用
 *
 * @author 李方捷
 */
public class Logger {

	/**
	 * 是否直接输出信息，而非使用 log 默认为false，使用 log 工具输出
	 */
	public static boolean DIRECT = false;

	private static Boolean HAS_IMPL=null;

	private static org.slf4j.Logger logger(StackTraceElement e) {
		org.slf4j.Logger lg=LoggerFactory.getLogger(e.getClassName());
		appendFramewokPackagesIf(lg);
		return lg;
	}
	/**
	 * 是否存在slf4j的实现
	 * @return 是否存在slf4j的实现
	 * */
	public static boolean hasImpl() {
		if(HAS_IMPL!=null) return HAS_IMPL.booleanValue();
		boolean has = false;
		try {
			org.slf4j.Logger lg=LoggerFactory.getLogger(Logger.class);
			appendFramewokPackagesIf(lg);
			if(lg instanceof NOPLogger) lg=null;
			has=lg!=null;
		} catch (Exception e) {
			System.err.println(StringUtil.toString(e));
		}
		HAS_IMPL=has;
		return has;
	}

	private static boolean IS_FRAMEWOK_PACKAGES_ADDED=false;

	private static void appendFramewokPackagesIf(org.slf4j.Logger lg) {

		if(IS_FRAMEWOK_PACKAGES_ADDED) return;
		try {
			//只识别logback
			Field f=lg.getClass().getDeclaredField("loggerContext");
			f.setAccessible(true);
			Object lc=f.get(lg);
			f=lc.getClass().getDeclaredField("frameworkPackages");
			f.setAccessible(true);
			lc=f.get(lc);
			ArrayList<String> lcc=(ArrayList<String>)lc;
			lcc.add(Logger.class.getPackage().getName());
			IS_FRAMEWOK_PACKAGES_ADDED=true;
		} catch (Exception e) {
			IS_FRAMEWOK_PACKAGES_ADDED=true;
		}

	}
	private static String format(Object string,Object... args)
	{
		return MessageFormatter.format(string.toString(), args).getMessage();
	}



	private static boolean directPrint(Object message,Object... args) {

		if(args.length>0)
		{
			message=format(message, args);
		}

		message = "["+DateUtil.getFormattedTime(false)+"] "+message;

		if(DIRECT )
		{
			System.out.println(message);
			return true;
		}
		//必须两个分支实现，否则会引起Looger初始化
		else if(!hasImpl())
		{
			System.out.println(message);
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean directError(Object message, Throwable throwable) {


		if(DIRECT)
		{
			System.err.println(message);
			if(throwable!=null) System.err.println(StringUtil.toString(throwable));
			return true;
		}
		else if(!hasImpl())
		{
			System.err.println(message);
			if(throwable!=null) System.err.println(StringUtil.toString(throwable));
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean directError(Object message) {

		 return directError(message,null);

	}

	/**
	 * @param message 日志信息
	 * */
	public static void info(Object message) {
		if (directPrint(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).info(handleMessage(e, message + ""));
	}

	/**
	 * @param marker marker
	*  @param message 日志信息
	 * */
	public static void info(Marker marker, Object message) {
		if (directPrint(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).info(marker, handleMessage(e, message == null ? null : message.toString()));
	}

	/**
	 * @param format format
	 * @param arg arg
	 */
	public static void info(String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).info(handleMessage(e, format), arg);
	}

	/**
	 * @param format format
	 * @param args args
	 */
	public static void info(String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).info(handleMessage(e, format), args);
	}

	/**
	 * @param message 日志信息
	 * @param throwable throwable
	 * */
	public static void info(String message, Throwable throwable) {
		if (directError(message, throwable)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).info(handleMessage(e, message), throwable);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg arg
	 * */
	public static void info(Marker marker, String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).info(marker, handleMessage(e, format), arg);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param args args
	 * */
	public static void info(Marker marker, String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).info(marker, handleMessage(e, format), args);
	}

	/**
	 * @param format format
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * */
	public static void info(String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).info(handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg1   arg1
	 * @param arg2   arg2
	 */
	public static void info(Marker marker, String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).info(marker, handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @return 是否开启info日志
	 * */
	public static boolean isInfoEnabled() {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isInfoEnabled();
	}

	/**
	 * @param marker marker
	 * @return 是否开启info日志
	 * */
	public static boolean isInfoEnabled(Marker marker) {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isInfoEnabled(marker);
	}

	//
	/**
	 * @param message 日志信息
	 * */
	public static void debug(String message) {
		if (directPrint(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(handleMessage(e, message));
	}

	/**
	 * @param marker marker
	 * @param message 日志信息
	 * */
	public static void debug(Marker marker, String message) {
		if (directPrint(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(marker, handleMessage(e, message));
	}

	/**
	 * @param format format
	 * @param arg arg
	 */
	public static void debug(String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(handleMessage(e, format), arg);
	}

	/**
	 * @param format format
	 * @param args args
	 */
	public static void debug(String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(handleMessage(e, format), args);
	}

	/**
	 * @param message 日志信息
	 * @param throwable throwable
	 * */
	public static void debug(String message, Throwable throwable) {
		if(directError(message,throwable)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(handleMessage(e, message), throwable);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg arg
	 * */
	public static void debug(Marker marker, String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(marker, handleMessage(e, format), arg);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param args args
	 * */
	public static void debug(Marker marker, String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(marker, handleMessage(e, format), args);
	}

	/**
	 * @param format format
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * */
	public static void debug(String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * */
	public static void debug(Marker marker, String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).debug(marker, handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @return 是否开启info日志
	 * */
	public static boolean isDebugEnabled() {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isDebugEnabled();
	}

	/**
	 * @param marker marker
	 * @return 是否开启debug日志
	 * */
	public static boolean isDebugEnabled(Marker marker) {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isDebugEnabled(marker);
	}

	//

	/**
	 * @param message 日志信息
	 * */
	public static void error(Object message) {
		if (directError(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).error(handleMessage(e, message + ""));
	}

	/**
	 * @param marker marker
	*  @param message 日志信息
	 * */
	public static void error(Marker marker, String message) {
		if(directError(message)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(marker, handleMessage(e, message));
	}

	/**
	 * @param format format
	 * @param arg arg
	 */
	public static void error(String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(handleMessage(e, format), arg);
	}

	/**
	 * @param format format
	 * @param args args
	 */
	public static void error(String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(handleMessage(e, format), args);
	}

	/**
	 * @param message 日志信息
	 * @param throwable throwable
	 * */
	public static void error(String message, Throwable throwable) {
		if (directError(message, throwable)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).error(handleMessage(e, message), throwable);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg arg
	 * */
	public static void error(Marker marker, String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(marker, handleMessage(e, format), arg);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param args args
	 * */
	public static void error(Marker marker, String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(marker, handleMessage(e, format), args);
	}

	/**
	 * @param format format
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * */
	public static void error(String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg1   arg1
	 * @param arg2   arg2
	 */
	public static void error(Marker marker, String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).error(marker, handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @return 是否开启error日志
	 * */
	public static boolean isErrorEnabled() {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isErrorEnabled();
	}

	/**
	 * @param marker marker
	 * @return 是否开启error日志
	 * */
	public static boolean isErrorEnabled(Marker marker) {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isErrorEnabled(marker);
	}

	//

	/**
	 * @param message 日志信息
	 * */
	public static void trace(Object message) {
		if (directPrint(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(handleMessage(e, message + ""));
	}

	/**
	 * @param marker marker
	*  @param message 日志信息
	 * */
	public static void trace(Marker marker, Object message) {
		if (directPrint(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(marker, handleMessage(e, message + ""));
	}

	/**
	 * @param format format
	 * @param arg arg
	 */
	public static void trace(String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(handleMessage(e, format), arg);
	}

	/**
	 * @param format format
	 * @param args args
	 */
	public static void trace(String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(handleMessage(e, format), args);
	}

	/**
	 * @param message 日志信息
	 * @param throwable throwable
	 * */
	public static void trace(String message, Throwable throwable) {
		if(directError(message,throwable)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(handleMessage(e, message), throwable);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg arg
	 * */
	public static void trace(Marker marker, String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(marker, handleMessage(e, format), arg);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param args args
	 * */
	public static void trace(Marker marker, String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(marker, handleMessage(e, format), args);
	}

	/**
	 * @param format format
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * */
	public static void trace(String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg1   arg1
	 * @param arg2   arg2
	 */
	public static void trace(Marker marker, String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).trace(marker, handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @return 是否开启trace日志
	 * */
	public static boolean isTraceEnabled() {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isTraceEnabled();
	}

	/**
	 * @param marker marker
	 * @return 是否开启trace日志
	 * */
	public static boolean isTraceEnabled(Marker marker) {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isTraceEnabled(marker);
	}

	//

	/**
	 * @param message 日志信息
	 * */
	public static void warn(Object message) {
		if (directError(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(handleMessage(e, message + ""));
	}

	/**
	 * @param marker marker
	*  @param message 日志信息
	 * */
	public static void warn(Marker marker, Object message) {
		if (directError(message)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(marker, handleMessage(e, message + ""));
	}

	/**
	 * @param format format
	 * @param arg arg
	 */
	public static void warn(String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(handleMessage(e, format), arg);
	}

	/**
	 * @param format format
	 * @param args args
	 */
	public static void warn(String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(handleMessage(e, format), args);
	}

	/**
	 * @param message 日志信息
	 * @param throwable throwable
	 * */
	public static void warn(String message, Throwable throwable) {
		if (directError(message, throwable)) {
			return;
		}
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(handleMessage(e, message), throwable);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg arg
	 * */
	public static void warn(Marker marker, String format, Object arg) {
		if(directPrint(format,arg)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(marker, handleMessage(e, format), arg);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param args args
	 * */
	public static void warn(Marker marker, String format, Object... args) {
		if(directPrint(format,args)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(marker, handleMessage(e, format), args);
	}

	/**
	 * @param format format
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * */
	public static void warn(String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @param marker marker
	 * @param format format
	 * @param arg1   arg1
	 * @param arg2   arg2
	 */
	public static void warn(Marker marker, String format, Object arg1, Object arg2) {
		if(directPrint(format,arg1,arg2)) return;
		StackTraceElement e = getStackTraceElement();
		logger(e).warn(marker, handleMessage(e, format), arg1, arg2);
	}

	/**
	 * @return 是否开启warn日志
	 * */
	public static boolean isWarnEnabled() {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isWarnEnabled();
	}

	/**
	 * @param marker marker
	 * @return 是否开启warn日志
	 * */
	public static boolean isWarnEnabled(Marker marker) {
		StackTraceElement e = getStackTraceElement();
		return logger(e).isWarnEnabled(marker);
	}

//	private static ThreadLocal<StringBuilder> builder=new ThreadLocal<>();

	private static StackTraceElement getStackTraceElement() {
		return Thread.currentThread().getStackTrace()[3];
	}

	private static String handleMessage(StackTraceElement e, String message) {
		return message;
	}



	/**
	 * 输出异常，同时info和error,保证异常可以被输出
	 * @param exception 异常
	 */
	public static void exception(Throwable exception) {
		if (DIRECT && exception != null) {
			exception.printStackTrace();
			return;
		}
		info("exception  : " + StringUtil.toString(exception));
		error("exception  : " + StringUtil.toString(exception));
	}

	/**
	 * 输出异常，同时info和error,保证异常可以被输出
	 * @param tag 异常标签，用于标记异常
	 * @param exception 异常
	 */
	public static void exception(String tag, Throwable exception) {
		if (DIRECT && exception != null) {
			exception.printStackTrace();
			return;
		}
		info(tag + " , exception  : " + StringUtil.toString(exception));
		error(tag + " , exception  : " + StringUtil.toString(exception));
	}

	public static final String TRACE_ID_KEY ="tid";

	/**
	 * 设置,日志串联ID
	 * @param tid  日志串联ID
	 * */
	public static void setTID(String tid) {
		MDC.put(TRACE_ID_KEY, tid);
	}

	public static String getTID() {
		return MDC.get(TRACE_ID_KEY);
	}



}
