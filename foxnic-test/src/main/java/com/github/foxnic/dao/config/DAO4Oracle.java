package com.github.foxnic.dao.config;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.spring.OracleDAO;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.expr.SQL;

public class DAO4Oracle extends OracleDAO implements TestDAO {
	public DAO4Oracle()
	{
		super();
		
		//设置默认方言
		GlobalSettings.DEFAULT_SQL_DIALECT=this.getSQLDialect();
		
		this.setDataSource((new Configs()).getDataSourceOracle());
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
	
	private String seqName="DEMO_SEQ";

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
 
		
		this.execute("CREATE SEQUENCE "+this.getSeqName()+" START WITH 1 INCREMENT BY 1 MINVALUE 0 NOCACHE  NOCYCLE NOORDER");
		
		
		createAllTypeTable();
		
		
	}
	
	private void createAllTypeTable() {
 
		String[] lines= {
			"CREATE TABLE "+this.getAllTypeTableName()+" (",
			"ID INTEGER NOT NULL",
			",DATE_value DATE,",
			"NUMBER_value_l1_s0 NUMBER(1,0),",
			"NUMBER_value_l1_s2 NUMBER(1,2),",
			"NUMBER_value_l2_s0 NUMBER(2,0),",
			"NUMBER_value_l2_s3 NUMBER(2,3),",
			"NUMBER_value_l3_s0 NUMBER(3,0),",
			"NUMBER_value_l3_s4 NUMBER(3,4),",
			"NUMBER_value_l4_s0 NUMBER(4,0),",
			"NUMBER_value_l4_s5 NUMBER(4,5),",
			"NUMBER_value_l5_s0 NUMBER(5,0),",
			"NUMBER_value_l5_s6 NUMBER(5,6),",
			"NUMBER_value_l6_s0 NUMBER(6,0),",
			"NUMBER_value_l6_s1 NUMBER(6,1),",
			"NUMBER_value_l7_s0 NUMBER(7,0),",
			"NUMBER_value_l7_s2 NUMBER(7,2),",
			"NUMBER_value_l8_s0 NUMBER(8,0),",
			"NUMBER_value_l8_s3 NUMBER(8,3),",
			"NUMBER_value_l9_s0 NUMBER(9,0),",
			"NUMBER_value_l9_s4 NUMBER(9,4),",
			"NUMBER_value_l10_s0 NUMBER(10,0),",
			"NUMBER_value_l10_s5 NUMBER(10,5),",
			"NUMBER_value_l11_s0 NUMBER(11,0),",
			"NUMBER_value_l11_s6 NUMBER(11,6),",
			"NUMBER_value_l12_s0 NUMBER(12,0),",
			"NUMBER_value_l12_s1 NUMBER(12,1),",
			"NUMBER_value_l13_s0 NUMBER(13,0),",
			"NUMBER_value_l13_s2 NUMBER(13,2),",
			"NUMBER_value_l14_s0 NUMBER(14,0),",
			"NUMBER_value_l14_s3 NUMBER(14,3),",
			"NUMBER_value_l15_s0 NUMBER(15,0),",
			"NUMBER_value_l15_s4 NUMBER(15,4),",
			"NUMBER_value_l16_s0 NUMBER(16,0),",
			"NUMBER_value_l16_s5 NUMBER(16,5),",
			"NUMBER_value_l17_s0 NUMBER(17,0),",
			"NUMBER_value_l17_s6 NUMBER(17,6),",
			"NUMBER_value_l18_s0 NUMBER(18,0),",
			"NUMBER_value_l18_s1 NUMBER(18,1),",
			"NUMBER_value_l19_s0 NUMBER(19,0),",
			"NUMBER_value_l19_s2 NUMBER(19,2),",
			"NUMBER_value_l20_s0 NUMBER(20,0),",
			"NUMBER_value_l20_s3 NUMBER(20,3),",
			"NUMBER_value_l21_s0 NUMBER(21,0),",
			"NUMBER_value_l21_s4 NUMBER(21,4),",
			"NUMBER_value_l22_s0 NUMBER(22,0),",
			"NUMBER_value_l22_s5 NUMBER(22,5),",
			"NUMBER_value_l23_s0 NUMBER(23,0),",
			"NUMBER_value_l23_s6 NUMBER(23,6),",
			"NUMBER_value_l24_s0 NUMBER(24,0),",
			"NUMBER_value_l24_s1 NUMBER(24,1),",
			"NUMBER_value_l25_s0 NUMBER(25,0),",
			"NUMBER_value_l25_s2 NUMBER(25,2),",
			"NUMBER_value_l26_s0 NUMBER(26,0),",
			"NUMBER_value_l26_s3 NUMBER(26,3),",
			"NUMBER_value_l27_s0 NUMBER(27,0),",
			"NUMBER_value_l27_s4 NUMBER(27,4),",
			"NUMBER_value_l28_s0 NUMBER(28,0),",
			"NUMBER_value_l28_s5 NUMBER(28,5),",
			"NUMBER_value_l29_s0 NUMBER(29,0),",
			"NUMBER_value_l29_s6 NUMBER(29,6),",
			"NUMBER_value_l30_s0 NUMBER(30,0),",
			"NUMBER_value_l30_s1 NUMBER(30,1),",
			"NUMBER_value_l31_s0 NUMBER(31,0),",
			"NUMBER_value_l31_s2 NUMBER(31,2),",
			"FLOAT_value_l4 FLOAT(4),",
			"BLOB_value BLOB,",
			"CLOB_value CLOB,",
			"TIMESTAMP_value_l6 TIMESTAMP(6),",
			"CHAR_value_l8 CHAR(8),",
			"VARCHAR2_value_l8 VARCHAR2(8),",
			"LONG_value LONG",
			")"
		};
		this.execute(SQL.joinSQLs(lines));
		
		//
		this.execute("COMMENT ON TABLE "+this.getAllTypeTableName()+" IS '联合主键表'");
		
		//
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".DATE_value IS 'DATE类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l1_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l1_s2 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l2_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l2_s3 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l3_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l3_s4 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l4_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l4_s5 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l5_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l5_s6 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l6_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l6_s1 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l7_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l7_s2 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l8_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l8_s3 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l9_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l9_s4 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l10_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l10_s5 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l11_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l11_s6 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l12_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l12_s1 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l13_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l13_s2 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l14_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l14_s3 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l15_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l15_s4 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l16_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l16_s5 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l17_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l17_s6 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l18_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l18_s1 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l19_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l19_s2 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l20_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l20_s3 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l21_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l21_s4 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l22_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l22_s5 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l23_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l23_s6 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l24_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l24_s1 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l25_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l25_s2 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l26_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l26_s3 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l27_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l27_s4 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l28_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l28_s5 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l29_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l29_s6 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l30_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l30_s1 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l31_s0 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".NUMBER_value_l31_s2 IS 'NUMBER类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".FLOAT_value_l4 IS 'FLOAT类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".BLOB_value IS 'BLOB类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".CLOB_value IS 'CLOB类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".TIMESTAMP_value_l6 IS 'TIMESTAMP类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".CHAR_value_l8 IS 'CHAR类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".VARCHAR2_value_l8 IS 'VARCHAR2类型字段'");
		this.execute("COMMENT ON COLUMN "+this.getAllTypeTableName()+".LONG_value IS 'LONG类型字段'");
		
		
	}
	

	private void createPKTable() {
		String[] lnsPK= {
				"CREATE TABLE "+getPKTableName(),
				"(",
				"BILL_ID   INTEGER,",
				 " OWNER_ID  INTEGER,",
				 " type      VARCHAR2(255)",
				")"
		};
		
		String[] pklnsPK= {
				"ALTER TABLE "+getPKTableName()+" ADD (",
				"  CONSTRAINT "+getPKTableName()+"_PK",
				"  PRIMARY KEY  (BILL_ID, OWNER_ID, TYPE)",
				"  ENABLE VALIDATE)"
		};
		
		this.execute(SQL.joinSQLs(lnsPK));
		this.execute(SQL.joinSQLs(pklnsPK));
		Logger.info("创建表 "+getPKTableName());
		
		//
		
		this.execute("COMMENT ON TABLE "+getPKTableName()+" IS '联合主键表'");
		
		this.execute("COMMENT ON COLUMN "+getPKTableName()+".BILL_ID IS '单据号'");
		this.execute("COMMENT ON COLUMN "+getPKTableName()+".OWNER_ID IS '所有者'");
		this.execute("COMMENT ON COLUMN "+getPKTableName()+".type IS '类型'");
	}

	private void createClobTable() {
		String[] lnsClob= {
				"CREATE TABLE "+getClobTableName(),
				"(",
				"  id           varchar2(64),",
				"  content         Clob",
				")"
		};
		
		String[] pklnsClob= {
				"ALTER TABLE "+getClobTableName()+" ADD (",
				"  CONSTRAINT "+getClobTableName()+"_PK",
				"  PRIMARY KEY  (id)",
				"  ENABLE VALIDATE)"
		};
		
		
		this.execute(SQL.joinSQLs(lnsClob));
		this.execute(SQL.joinSQLs(pklnsClob));
		
		this.execute("COMMENT ON TABLE "+getClobTableName()+" IS '大内容表'");
		//
		this.execute("COMMENT ON COLUMN "+getClobTableName()+".id IS 'ID'");
		this.execute("COMMENT ON COLUMN "+getClobTableName()+".content IS '代码,业务代码'");
		
		Logger.info("创建表 "+getClobTableName());
	}

	private void createNewsTable() {
		String[] lns= {
				"CREATE TABLE "+getNormalTableName(),
				"(",
				"  id           INTEGER,",
				"  code         VARCHAR2(36),",
				"  title        VARCHAR2(255),",
				"  publish_day  DATE,",
				"  enter_time   TIMESTAMP(6),",
				"  newsId         VARCHAR2(64),",
				"  read_times   INTEGER,",
				"  alert_time   TIMESTAMP(6),",
				"  price        NUMBER(20,2),",
				"  create_by    VARCHAR2(36),",
				"  create_time  DATE,",
				"  deleted      INTEGER",
				")"
		};
		
		String[] pklns= {
				"ALTER TABLE "+getNormalTableName()+" ADD (",
				"  CONSTRAINT "+getNormalTableName()+"_PK",
				"  PRIMARY KEY  (id)",
				"  ENABLE VALIDATE)"
		};
		
		this.execute(SQL.joinSQLs(lns));
		this.execute(SQL.joinSQLs(pklns));
		Logger.info("创建表 "+getNormalTableName());
	 
		this.execute("COMMENT ON TABLE "+getNormalTableName()+" IS '新闻 新闻信息'");
		//
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".id IS 'ID'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".code IS '代码,业务代码'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".title IS '标题'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".publish_day IS '发布时间，新闻的实际发布时间'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".enter_time IS '输入时间，开始录入的时间'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".newsId IS '新闻内容ID'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".alert_time IS '提醒时间'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".read_times IS '阅读次数'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".price IS '单价，阅读计费'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".create_by IS '创建人'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".create_time IS '创建时间'");
		this.execute("COMMENT ON COLUMN "+getNormalTableName()+".deleted IS '是否已删'");
		 
 
	}

	@Override
	public void dropTables() {

		if(this.isTableExists(getNormalTableName())) {
			Logger.info("删除表 "+getNormalTableName());
			this.execute("DROP TABLE "+getNormalTableName());
		}
		if(this.isTableExists(getPKTableName())) {
			Logger.info("删除表 "+getPKTableName());
			this.execute("DROP TABLE "+getPKTableName());
		}
		
		if(this.isTableExists(getClobTableName())) {
			Logger.info("删除表 "+getClobTableName());
			this.execute("DROP TABLE "+getClobTableName());
		}
		
		if(this.isTableExists(getAllTypeTableName())) {
			Logger.info("删除表 "+getAllTypeTableName());
			this.execute("DROP TABLE "+getAllTypeTableName());
		}
		
		try {
			this.execute("DROP SEQUENCE "+this.getSeqName());
		} catch (Exception e) {}
		 
	}


	public String getSeqName() {
		return seqName;
	}
}
