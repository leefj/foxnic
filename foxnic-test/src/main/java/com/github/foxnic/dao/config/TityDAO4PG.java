package com.github.foxnic.dao.config;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.spring.MySqlDAO;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.expr.SQL;

public class TityDAO4PG extends MySqlDAO implements TestDAO{
	public TityDAO4PG()
	{
		super();
		
		//设置默认方言
		GlobalSettings.DEFAULT_SQL_DIALECT=this.getSQLDialect();
		
		this.setDataSource((new Configs()).getDataSourcePG());
	}
 
	private String normalTable=null;
	private String pkTable=null;
	private String clobTableName=null;
	
	private String allTypeTableName=null;
	
	public String getAllTypeTableName() {
		return allTypeTableName;
	}
	 
	public String getClobTableName() {
		return clobTableName;
	}
	 
	public String getNormalTableName() {
		return normalTable;
	}
	
	public String getPKTableName() {
		return pkTable;
	}

	public void setTableName(String normalTable,String pkTable,String clobTable,String allTypeTable) {
		this.normalTable = normalTable;
		this.pkTable=pkTable;
		this.clobTableName=clobTable;
		this.allTypeTableName=allTypeTable;
	}
 
	@Override
	public void createTables() {

		dropTables();
		
		createNewsTable();
		
		createClobTable();
		
		createPKTable();
		
		createAllTypeTable();
		
	}

	private void createPKTable() {
		
		String[] lnsPK = { 
				"CREATE TABLE `"+this.getPKTableName()+"`  (", 
				" `bill_id` int(0) NOT NULL COMMENT '单据号',",
				" `owner_id` int(0) NOT NULL COMMENT '所有者',", 
				"`type` varchar(255) NOT NULL COMMENT '类型',",
				"PRIMARY KEY (`bill_id`, `owner_id`, `type`)", 
				") COMMENT = '主键测试表；联合主键表'"
		};

		this.execute(SQL.joinSQLs(lnsPK));
		Logger.info("创建表 "+getPKTableName());
	}
	
	private void createAllTypeTable() {
		
		
		String[] lines= {
			"CREATE TABLE "+this.getAllTypeTableName()+" (",
			"ID int(11) NOT NULL COMMENT 'ID',BIT_value_l8 BIT(8) COMMENT 'BIT类型字段',",
			"TINYINT_value_l6 TINYINT(6) COMMENT 'TINYINT类型字段',",
			"SMALLINT_value_l6 SMALLINT(6) COMMENT 'SMALLINT类型字段',",
			"MEDIUMINT_value_l6 MEDIUMINT(6) COMMENT 'MEDIUMINT类型字段',",
			"INT_value_l6 INT(6) COMMENT 'INT类型字段',",
			"BIGINT_value_l6 BIGINT(6) COMMENT 'BIGINT类型字段',",
			"FLOAT_value_l6_s2 FLOAT(6,2) COMMENT 'FLOAT类型字段',",
			"DOUBLE_value_l6_s2 DOUBLE(6,2) COMMENT 'DOUBLE类型字段',",
			"DECIMAL_value_l6_s2 DECIMAL(6,2) COMMENT 'DECIMAL类型字段',",
			"CHAR_value_l64 CHAR(64) COMMENT 'CHAR类型字段',",
			"VARCHAR_value_l64 VARCHAR(64) COMMENT 'VARCHAR类型字段',",
			"TINYTEXT_value TINYTEXT COMMENT 'TINYTEXT类型字段',",
			"TEXT_value TEXT COMMENT 'TEXT类型字段',",
			"MEDIUMTEXT_value MEDIUMTEXT COMMENT 'MEDIUMTEXT类型字段',",
			"LONGTEXT_value LONGTEXT COMMENT 'LONGTEXT类型字段',",
			"ENUM_value ENUM('A','B','C','D') COMMENT 'ENUM类型字段',",
			"SET_value SET('A','B','C','D') COMMENT 'SET类型字段',",
			"TIME_value TIME COMMENT 'TIME类型字段',",
			"DATE_value DATE COMMENT 'DATE类型字段',",
			"DATETIME_value DATETIME COMMENT 'DATETIME类型字段',",
			"TIMESTAMP_value TIMESTAMP COMMENT 'TIMESTAMP类型字段',",
			"YEAR_value YEAR COMMENT 'YEAR类型字段',",
			"BLOB_value BLOB COMMENT 'BLOB类型字段',",
			"LONGBLOB_value LONGBLOB COMMENT 'LONGBLOB类型字段'",
			",  PRIMARY KEY (ID) USING BTREE",
			") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT = '全类型，用于类型测试的表'"
		};

		this.execute(SQL.joinSQLs(lines));
		Logger.info("创建表 "+getAllTypeTableName());
	}

	
	private void createNewsTable() {
		
		String[] lns= {
				"CREATE TABLE `"+getNormalTableName()+"` (",
				"  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',",
				"  `code` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '代码,业务代码',",
				"  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '标题',",
				"  `publish_day` datetime DEFAULT NULL COMMENT '发布时间，新闻的实际发布时间',",
				"  `enter_time` timestamp(6) NULL DEFAULT NULL COMMENT '输入时间，开始录入的时间',",
				"  `newsId` varchar(64) COMMENT '新闻内容ID',",
				"  `alert_time`  time(3) COMMENT '提醒时间',",
				"  `read_times` int(11) DEFAULT NULL COMMENT '阅读次数',",
				"  `price` decimal(10,2) DEFAULT NULL COMMENT '单价，阅读计费',",
				"  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建人',",
				"  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',",
				"  `deleted` int(1) NULL DEFAULT NULL COMMENT '是否已删',",
				"  `valid` int(1) NULL DEFAULT NULL COMMENT '是否有效',",
				"  `is_used` int(1) NULL DEFAULT NULL COMMENT '是否有效',",
				"  `is_active` varchar(1) NULL DEFAULT NULL COMMENT '是否激活',",
				"  `running` varchar(1) NULL DEFAULT NULL COMMENT '是否运行中',",
				"  PRIMARY KEY (`id`) USING BTREE",
				") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT = '新闻 新闻信息'",
		};
		
		this.execute(SQL.joinSQLs(lns));
		Logger.info("创建表 "+getNormalTableName());
	}
	
	private void createClobTable() {
		
		String[] lns= {
				"CREATE TABLE `"+getClobTableName()+"` (",
				"  `id` varchar(64) NOT NULL COMMENT 'ID',",
				"  `content` longtext DEFAULT NULL COMMENT '内容',",
				" PRIMARY KEY (`id`)",
				") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT = '内容表，大内容部分存储'",
		};
		
		this.execute(SQL.joinSQLs(lns));
		Logger.info("创建表 "+getClobTableName());
		
	}
	
	

	@Override
	public void dropTables() {
		Logger.info("删除表 "+getNormalTableName());
		this.execute("DROP TABLE IF EXISTS `"+getNormalTableName()+"`");
		Logger.info("删除表 "+getPKTableName());
		this.execute("DROP TABLE IF EXISTS `"+getPKTableName()+"`");
		Logger.info("删除表 "+getClobTableName());
		this.execute("DROP TABLE IF EXISTS `"+getClobTableName()+"`");
		Logger.info("删除表 "+getAllTypeTableName());
		this.execute("DROP TABLE IF EXISTS `"+getAllTypeTableName()+"`");
	}
}
