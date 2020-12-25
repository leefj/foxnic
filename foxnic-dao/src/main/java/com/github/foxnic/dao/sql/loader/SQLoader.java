package com.github.foxnic.dao.sql.loader;

import java.util.HashMap;

import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBType;


public class SQLoader  {
	
	private SQLoader() {}
	
	
	private static TQLCache SQL_CACHE=null;
	
	private static SimpleTaskManager TASK_MGR=new SimpleTaskManager(1);
	private static int TASK_ID=-1;
	
	public static boolean isReady()
	{
		return TQLCache.instance().isScaneCompleted();
	}
	
	/**
	 * PKG缓存
	 * */
	private static HashMap<DAO, String[]> SCAN_PACKAGES=new HashMap<DAO, String[]>();
	
	
	
	public static void setTQLScanPackage(DAO dao,String... packages)
	{
		SCAN_PACKAGES.put(dao, packages);
		setTQLScans(packages);
	}
	
	
	/**
	 * 设置TQL扫描范围，仅在程序启动后第一次调用时有效
	 * @param packages 包，扫描范围
	 * */
	private static void setTQLScans(String... packages)
	{
		//这样设计的目的是只允许设置一次
		if(SQL_CACHE==null)
		{
			SQL_CACHE=TQLCache.instance();
		}
		SQL_CACHE.addPackages(packages);
 
		TASK_MGR.clearTask(TASK_ID);
		
		Runnable r=new Runnable() {
			@Override
			public void run() {
				//触发扫描
				SQL_CACHE.get("#null",DBType.MYSQL);
			}
		};
		TASK_ID=TASK_MGR.doDelayTask(r, 1);
	}
	
	
	/**
	 * 获得指定类型的SQL
	 * @param id 语句ID
	 * @param dbType 数据类型
	 * @return 语句
	 * */
	public static String getSQL(String id,DBType dbType)
	{
		id=id.trim();
		//如果是多行将识别为语句而非id
		if(id.contains("\n") || id.contains("\r") || id.contains("\t"))
		{
			return id;
		}
		
		TQL tql=SQL_CACHE.get(id,dbType);
		if(tql==null) return null;
		return tql.getSql();
	}
	
	/**
	 * 获得指定类型的SQL
	 * @param id 语句ID
	 * @param dao DAO
	 * @return 语句
	 * */
	public static String getSQL(String id,DAO dao)
	{
		id=id.trim();
		//如果是多行将识别为语句而非id
		if(id.contains("\n") || id.contains("\r") || id.contains("\t")){
			return id;
		}
		TQL tql=SQL_CACHE.get(id,dao.getDBType());
		if(tql==null) return null;
		//校验DAO的SQL范围
		//暂不实现
//		String[] pkgs=SCAN_PACKAGES.get(dao);
//		if(pkgs!=null || pkgs.length>0)
//		{
//			
//		}
		return tql.getSql();
	}
}
