package com.github.foxnic.dao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * Hello world!
 *
 */

@Configuration
public class Configs 
{
	
	public static final String ORACLE_PASSWD = "123456";
	public static final String ORACLE_USER = "aux_pms";
	public static final String ORACLE_URL = "jdbc:oracle:thin:@//10.88.2.56:1521/pms";
	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	//
	public static final String MYSQL_PASSWD = "foxnic";
	public static final String MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/foxnic_test_db?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&allowPublicKeyRetrieval=true";
	public static final String MYSQL_USER = "foxnic";
	public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	public static DAO dao=null;
	/**
	 * 通过此处控制需要测试的数据库类型
	 * */
	public static DAO getDAO()
	{
		if(dao==null) dao=new TityDAO4MySQL();
//		if(dao==null) dao=new TityDAO4Oracle();
//		if(dao==null) dao=new TityDAO4Db2();
		
		Logger.info("use db type : "+dao.getDBType().name());
		
		return dao;
	}
	
	
	public static DAO getDAO(SQLDialect dialect)
	{
		if(dialect==SQLDialect.PLSQL) return new TityDAO4Oracle();
		if(dialect==SQLDialect.MySQL) return new TityDAO4MySQL();
		if(dialect==SQLDialect.DB2) return new TityDAO4Db2();
		return null;
	}
 
 
	public DruidDataSource getDataSourceOracle() {
 
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setDriverClassName(ORACLE_DRIVER);
 
		dataSource.setUrl(ORACLE_URL);
		dataSource.setUsername(ORACLE_USER);
		dataSource.setPassword(ORACLE_PASSWD);
//		dataSource.setTestOnBorrow(true);
		dataSource.setPoolPreparedStatements(true); // mysql 关闭，Oracle 建议开启
//		dataSource.setTestWhileIdle(true); // 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		return dataSource;
	}
	
	/**
	 * 设置数据源
	 * */
	@Bean(name="ds")
	public  DruidDataSource getLocalTityTestDataSource() {
		
		DruidDataSource dataSource=new DruidDataSource();
//    	dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    	dataSource.setDriverClassName(MYSQL_DRIVER);
    	dataSource.setUsername(MYSQL_USER);
//    	dataSource.setMinIdle(8);
//    	dataSource.setMaxActive(1024);
    	dataSource.setUrl(MYSQL_URL);
//    	dataSource.setPassword("LeeFJ@aux2018");
//    	dataSource.setUrl("jdbc:mysql://localhost:3306/titydb?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true");
    	dataSource.setPassword(MYSQL_PASSWD);
//    	dataSource.setTestOnBorrow(true);
//    	dataSource.setValidationQuery("select 1");
//    	dataSource.setPoolPreparedStatements(false);  //mysql  关闭，Oracle 建议开启
//    	dataSource.setTestWhileIdle(true); // 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		return dataSource;
	}
	
	
//	/**
//	 * 设置数据源
//	 * */
//	@Bean(name="sequenceds")
//	public  DruidDataSource getSequenceDataSource() {
//		
//		
//		 
//		// 参考文章 https://www.cnblogs.com/wuyun-blog/p/5679073.html
//		
//		DruidDataSource dataSource=new DruidDataSource();
////    	dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//    	dataSource.setDriverClassName(MYSQL_DRIVER);
//    	dataSource.setUsername(MYSQL_USER);
//    	
////    	dataSource.setMinIdle(8);
////    	dataSource.setMaxActive(1024);
//    	
//    	dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/titydb_test?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&zeroDateTimeBehavior=convertToNull");
////    	dataSource.setPassword("LeeFJ@aux2018");
//    	
////    	dataSource.setUrl("jdbc:mysql://localhost:3306/titydb?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true");
//    	dataSource.setPassword(MYSQL_PASSWD);
////    	dataSource.setTestOnBorrow(false);
////    	dataSource.setPoolPreparedStatements(false);  //mysql  关闭，Oracle 建议开启
////    	dataSource.setTestWhileIdle(true); // 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
//		return dataSource;
//	}
 
//    public DruidDataSource getDataSourceDMS() {
// 
//		DruidDataSource dataSource = new DruidDataSource();
//		dataSource.setDriverClassName(ORACLE_DRIVER);
// 
//		dataSource.setUrl("jdbc:oracle:thin:@100.100.0.201:1521:orcl");
//		dataSource.setUsername("dms_user");
//		dataSource.setPassword("dms_user");
//
////		dataSource.setTestOnBorrow(true);
//		dataSource.setPoolPreparedStatements(true); // mysql 关闭，Oracle 建议开启
////		dataSource.setTestWhileIdle(true); // 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
//		return dataSource;
//	}
    
    
    
    public DruidDataSource getDataSourceSampleDb2() {
    	 
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
 
//		dataSource.setUrl("jdbc:db2://47.92.240.43:50000/sample");
		dataSource.setUrl("jdbc:db2://47.92.240.43:50000/sample:currentSchema=DB2INST1;driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;");
//		dataSource.setUrl("jdbc:db2://47.92.240.43:50000/sample:currentSchema=DB2INST1;");
//		dataSource.setUrl("jdbc:db2://47.92.240.43:50000/sample:currentSchema=TITYDB;");
		dataSource.setUsername("db2inst1");
		dataSource.setPassword("oracle123oracle");

//		dataSource.setTestOnBorrow(true);
		dataSource.setPoolPreparedStatements(true); // mysql 关闭，Oracle 建议开启
//		dataSource.setTestWhileIdle(true); // 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		return dataSource;
	}
	
}

 





