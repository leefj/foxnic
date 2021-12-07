package com.github.foxnic.commons.lang;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日期工具
 * @author lifangjie
 * */
public class DateUtil {

	public final static long MICRO_SECOND = 1;

	/** The Constant SECOND. 一秒钟的毫秒数 */
	public final  static long SECOND = 1000;

	/** The Constant MINUTE. 一秒钟的毫秒数*/
	public final  static long MINUTE = 60000;

	/** The Constant HOUR.一小时的毫秒数 */
	public final  static long HOUR = 3600000;

	/** The Constant DAY. 一天的毫秒数*/
	public final  static long DAY = 86400000;

	/** The Constant WEEK. 一周的毫秒数*/
	public final  static long WEEK = 604800000;



	private static final ZoneId zone = ZoneId.systemDefault();

	/**
	 * 将 Date 转换成 LocalDateTime
	 * @param date date
	 * @return LocalDateTime
	 * */
	public static LocalDateTime toLocalDateTime(Date date)
	{
		 Instant instant = date.toInstant();
		 return LocalDateTime.ofInstant(instant, zone);
	}



	/**
	 * 将 Date 转换成 LocalDate
	 * @param date date
	 * @return LocalDate
	 * */
	public static LocalDate toLocalDate(Date date)
	{
		 return toLocalDateTime(date).toLocalDate();
	}


	/**
	 * 将 Date 转换成 LocalTime
	 * @param date date
	 * @return LocalTime
	 * */
	public static LocalTime toLocalTime(Date date)
	{
		if(date instanceof Time) {
			date=new Date(date.getTime());
		}
		return toLocalDateTime(date).toLocalTime();
	}

	/**
	 * 将 LocalDateTime 转换成 Date
	 * @param datetime LocalDateTime
	 * @return Date
	 * */
	public static Date toDate(LocalDateTime datetime)
	{
		ZonedDateTime zdt = datetime.atZone(zone);
		return Date.from(zdt.toInstant());
	}

	/**
	 * 将 LocalDateTime 转换成 Date
	 * @param localDate LocalDate
	 * @return Date
	 * */
	public static Date toDate(LocalDate localDate)
	{
		Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
		return Date.from(instant);
	}

	/**
	 * 将 LocalTime 转换成 Date
	 * @param localTime LocalTime
	 * @return Date
	 * */
	public static Date toDate(LocalTime localTime)
	{
		LocalDate localDate = LocalDate.now();
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
		Instant instant = localDateTime.atZone(zone).toInstant();
		return Date.from(instant);
	}


	//日期转换

	private static final String[] D_FMT= {"yyyy-M-d","yyyy-MM-dd","yyyy-M-dd","yyyy-MM-d","yyyy/MM/dd","yyyy/M/dd","yyyy/M/d","yyyy/MM/d","yyyyMMdd"};
	private static final String[] T_FMT= {"HH:mm:ss","HHmmss","HH:mm","HHmm","HH"};
	private static final String FMT_CHARS= "ymdHmsM";
	/**
	 * 循环时存在并发异常，修改为 ConcurrentHashMap 类型
	 * */
	private static final Map<String, SimpleDateFormat> FMT_MAP=new ConcurrentHashMap<>();

	/**
	 * 以下字符将被替换为空格
	 * */
	private static final String SPACE_CHARS= "\t\nT　Z\r";

	private static final char COLON_FULL= '：';
	private static final char COLON_HALF= ':';
	private static final char SPACE_1_CHAR= ' ';
	private static final String SPACE_1_STR= " ";
	private static final String SPACE_2= "  ";

	private static String dealDateStr(String value)
	{
		if(value!=null) value=value.trim();
		for (int i = 0; i < SPACE_CHARS.length(); i++) {
			char c=SPACE_CHARS.charAt(i);
			while(value.indexOf(c)!=-1)
			{
				value=value.replace(c, SPACE_1_CHAR);
			}
		}

		while(value.indexOf(COLON_FULL)!=-1)
		{
			value=value.replace(COLON_FULL, COLON_HALF);
		}
		//将两个空格替换为一个空格
		while(value.indexOf(SPACE_2)!=-1)
		{
			value=value.replaceAll(SPACE_2, SPACE_1_STR);
		}
		value=value.trim();
		return value;
	}

	private static boolean checkFormat(String val, String fmt) {

		int i=val.length();
		int j=fmt.length();
		if(i!=j) {
			return false;
		}


		i=fmt.indexOf(' ');
		j=val.indexOf(' ');
		if(i!=j) {
			return false;
		}

		i=fmt.indexOf('-');
		j=val.indexOf('-');
		if(i!=j) {
			return false;
		}

		i=fmt.indexOf('/');
		j=val.indexOf('/');
		if(i!=j) {
			return false;
		}

		i=fmt.indexOf(':');
		j=val.indexOf(':');
		if(i!=j) {
			return false;
		}

		i=fmt.indexOf('.');
		j=val.indexOf('.');
		if(i!=j) {
			return false;
		}

		char fc;
		char vc;
		for (int k = 0; k < fmt.length(); k++) {
			fc=fmt.charAt(k);
			vc=val.charAt(k);
			if(FMT_CHARS.indexOf(fc)==-1 && fc!=vc) {
					return false;
			}
		}
		return true;

	}

	private synchronized static void makeFormatsIf() {
		if(FMT_MAP.size()>0) {
			return;
		}


		String fmt=null;
		SimpleDateFormat sdf=null;

		fmt="yyyy-MM";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);

		fmt="yyyy-M";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);

		fmt="yyyy/MM";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);

		fmt="yyyy/M";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);

		fmt="yyyyMM";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);

		fmt="yyyyM";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);

		fmt="yyyy";
		sdf = new SimpleDateFormat(fmt);
		FMT_MAP.put(fmt, sdf);


		for (String d : D_FMT) {
			for (String t : T_FMT) {

				fmt=d;
				sdf = new SimpleDateFormat(d);
				FMT_MAP.put(fmt, sdf);

				fmt=d+" "+t;
				sdf = new SimpleDateFormat(fmt);
				FMT_MAP.put(fmt, sdf);

				fmt=d+" "+"H";
				sdf = new SimpleDateFormat(fmt);
				FMT_MAP.put(fmt, sdf);

				fmt=d+t;
				sdf = new SimpleDateFormat(fmt);
				FMT_MAP.put(fmt, sdf);

			}
		}
	}

	/**
	 * 字符串转日期
	 * @param value 字符串
	 * @return Date
	 * */
	public static Date parse(String value) {
		value=value.trim();
		makeFormatsIf();
		value = dealDateStr(value);
		Date datetime = null;
		for (Entry<String, SimpleDateFormat> en : FMT_MAP.entrySet()) {
			if (checkFormat(value, en.getKey())) {
				SimpleDateFormat fmt = FMT_MAP.get(en.getKey());
				try {
					datetime = fmt.parse(value);
					break;
				} catch (Exception e) {
					datetime = null;
				}
			}
		}
		return datetime;
	}

	private static HashMap<String,SimpleDateFormat> FORMATS=new HashMap<String,SimpleDateFormat>();

	/**
	 * 格式化日期 G Era标志符 Text 示例：AD <br>
	 * y 年 Year 示例：1996; 96 <br>
	 * M 年中的月份 Month 示例：July; Jul;07 <br>
	 * w 年中的周数 Number 示例：27 <br>
	 * W 月份中的周数 Number 示例：2 <br>
	 * D 年中的天数 Number 示例：189 <br>
	 * d 月份中的天数 Number 示例：10 <br>
	 * F 月份中的星期 Number 示例：2 <br>
	 * E 星期中的天数 Text 示例：Tuesday; Tue <br>
	 * a Am/pm 标记 Text 示例：PM <br>
	 * H 一天中的小时数 （0-23） Number 示例：0 <br>
	 * k 一天中的小时数 （1-24） Number 示例：24 <br>
	 * K am/pm 中的小时数 （0-11） Number 示例：0 <br>
	 * h am/pm 中的小时数 （1-12） Number 示例：12 <br>
	 * m 小时中的分钟数 Number 示例：30 <br>
	 * s 分钟中的秒数 Number 示例：55 <br>
	 * S 毫秒数 Number 示例：978 <br>
	 * z 时区 General time zone 示例：Pacific Standard Time; PST; GMT-08:00 <br>
	 * Z 时区 RFC 822 time zone 示例：-0800 <br>
	 *
	 * @param date   日期
	 * @param format 格式
	 * @return 格式化后的字符串
	 */
	public static String format(Date date, String format)
	{
		SimpleDateFormat formatter=FORMATS.get(format);
		if(formatter==null) {
			formatter=new SimpleDateFormat(format);
			FORMATS.put(format,formatter);
		}

		String mDateTime;
		try {
			mDateTime = formatter.format(date);
		} catch (Exception e) {
			//多试几次
			try {
				formatter.applyPattern(format);
				mDateTime = formatter.format(date);
			} catch (Exception e1) {
				 return format(date,"yyyy-MM-dd HH:mm:ss");
			}
		}
		return mDateTime;
	}


	/**
	 * Current time millis.
	 *
	 * @return the long
	 */
	public static long currentTimeMillis()
	{
		return System.currentTimeMillis();
	}

	/**
	 * Nano time.
	 *
	 * @return the long
	 */
	public static long nanoTime()
	{
		return System.nanoTime();
	}

	/**
	 * 格式化当前的日期时间,格式 yyyy-MM-dd HH:mm:ss
	 * @param cn 是否使用中文格式化
	 * @return 格式化后的日期
	 */
	public static String getFormattedTime(boolean cn)
	{
		String pattern = null;
		if(cn) {
			pattern="yyyy年MM月dd日 HH点mm分ss秒";
		} else {
			pattern="yyyy-MM-dd HH:mm:ss";
		}
		return format(new Date(),pattern);
	}

	public static void main(String[] args) {
		System.out.println(DateUtil.getFormattedTime(false));
	}

	/**
	 * 格式化当前的日期时间，格式 yyyy-MM-dd
	 * @param cn 是否使用中文格式化
	 * @return 格式化后的日期
	 */
	public static String getFormattedDate(boolean cn)
	{
		String pattern = null;
		if(cn) {
			pattern="yyyy年MM月dd日";
		} else {
			pattern="yyyy-MM-dd";
		}
		return format(new Date(),pattern);
	}


	public static String getCurrTime(String format)
	{
		Calendar cal = Calendar.getInstance();
		return format(cal.getTime(), format);
	}

	public static int getCurrentYear()
	{
		return Integer.parseInt(getCurrTime("yyyy"));
	}

	public static int getCurrentMonth()
	{
		return Integer.parseInt(getCurrTime("MM"));
	}

	public static int getYearPart(Date datetime)
	{
		return Integer.parseInt(format(datetime, "yyyy"));
	}
	public static int getMonthPart(Date datetime)
	{
		return Integer.parseInt(format(datetime, "MM"));
	}
	public static int getDayPart(Date datetime)
	{
		return Integer.parseInt(format(datetime, "dd"));
	}
	public static int getHourPart(Date datetime)
	{
		return Integer.parseInt(format(datetime, "HH"));
	}
	public static int getMinutePart(Date datetime)
	{
		return Integer.parseInt(format(datetime, "mm"));
	}
	public static int getSecondPart(Date datetime)
	{
		return Integer.parseInt(format(datetime, "ss"));
	}

	/**
	 * 获得指定日期在w周后的日期；如，date是星期二，那么么w周后的星期二是几号<br>
	 * 即date加上w周的时间得到的日期
	 *
	 * @param date the date
	 * @param w 周数
	 * @return the same day by week
	 */
	public static Date getSameDayByWeek(Date date, int w)
	{
		long myTime = date.getTime() + WEEK * w;
		date.setTime(myTime);
		return date;
	}

	private static final String dayNames[] = { "一", "二", "三", "四", "五", "六", "日" };

	/**
	 * Gets the chinese week.
	 *
	 * @param date the date
	 * @param shorter 如果true 返回  周一、周二格式，如果false 返回 星期一、星期二格式
	 * @return the chinese week
	 */
	public static String getChineseWeek(Calendar date,boolean shorter)
	{
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		return (shorter?"周":"星期")+dayNames[((dayOfWeek - 2)+7)%7];
	}


	/**
	 * 是否周末
	 */
	public static boolean isWeekEnd(Calendar date)
	{
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		dayOfWeek=((dayOfWeek - 2)+7)%7;
		return dayOfWeek>=5;
	}

	/**
	 * 是否周末
	 */
	public static boolean isWeekEnd(Date date)
	{
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return isWeekEnd(cal);
	}

	/**
	 * Gets the chinese week.
	 *
	 * @param date the date
	 * @param shorter 如果true 返回  周一、周二格式，如果false 返回 星期一、星期二格式
	 * @return the chinese week
	 */
	public static String getChineseWeek(Date date,boolean shorter)
	{
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return getChineseWeek(cal,shorter);
	}


	/**
	 * Checks if is same day.
	 *
	 * @param d1 the d 1
	 * @param d2 the d 2
	 * @return true, if is same day
	 */
	public static boolean isSameDay(Date d1,Date d2)
	{
		Calendar c1=Calendar.getInstance();
		c1.setTime(d1);

		Calendar c2=Calendar.getInstance();
		c2.setTime(d2);
		return c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH)==c2.get(Calendar.MONTH) && c1.get(Calendar.DATE)==c2.get(Calendar.DATE);
	}

	/**
	 * Checks if  two date in same week.
	 *
	 * @param d1 the d 1
	 * @param d2 the d 2
	 * @return true, if is same date
	 */
	public static boolean isInSameWeek(Date d1, Date d2) {

		if(d1==null || d2==null) return false;

		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		// 西方周日为一周的第一天，咱得将周一设为一周第一天
		cal1.setFirstDayOfWeek(Calendar.MONDAY);
		cal2.setFirstDayOfWeek(Calendar.MONDAY);
		cal1.setTime(d1);
		cal2.setTime(d2);

		int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);

		if (subYear == 0) {

			// subYear==0,说明是同一年
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;

		}  else if (subYear == 1 && cal2.get(Calendar.MONTH) == 11)
		{
			 // subYear==1,说明cal比cal2大一年;java的一月用"0"标识，那么12月用"11"
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;

		} else if (subYear == -1 && cal1.get(Calendar.MONTH) == 11) {

			// subYear==-1,说明cal比cal2小一年
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		}
		return false;
	}



	private static Date add(final Date date, final int calendarField, final int amount) {
		if(date==null) {
			throw new IllegalArgumentException("date is null");
		}
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

	/**
     * Adds a number of years to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addYears(final Date date, final int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of months to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addMonths(final Date date, final int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of weeks to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addWeeks(final Date date, final int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of days to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addDays(final Date date, final int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of hours to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addHours(final Date date, final int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of minutes to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addMinutes(final Date date, final int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of seconds to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addSeconds(final Date date, final int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a number of milliseconds to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addMilliseconds(final Date date, final int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }

	/**
	 * 把日期转换为当天的0时0分0秒
	 * */
    public static Date dayFloor(Date date) {
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(date);
		calendarStart.set(calendarStart.get(Calendar.YEAR),
				calendarStart.get(Calendar.MONTH),
				calendarStart.get(Calendar.DATE),
				0, 0, 0);
		date = calendarStart.getTime();
		return date;
    }
}
