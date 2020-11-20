package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.sql.parameter.MapParamBuilder;

public class ExprTest {
	
	@Test
	public void test_se_empty()
	{
		Expr se0=new Expr("\n",8);
		System.out.println(se0.isEmpty());
		assertTrue(se0.isEmpty());
		Expr se1=new Expr("a=b",8);
		System.out.println(se1.isEmpty());
		assertTrue(!se1.isEmpty());
		se0=se0.append("\n").append(" and b=?",90);
		System.out.println(se0.isEmpty());
		assertTrue(!se0.isEmpty());
		 
	}
	
	@Test
	public void test_se_1()
	{
		Expr se0=new Expr("\n",8);
		System.out.println("!"+se0.getSQL()+"|");
		System.out.println("!"+se0.getListParameterSQL()+"|");
		System.out.println("!"+se0.getNamedParameterSQL()+"|");
		assertTrue(se0.getSQL().isEmpty());
		assertTrue(se0.getListParameterSQL().isEmpty());
		assertTrue(se0.getNamedParameterSQL().isEmpty());
		
		assertTrue(se0.getNamedParameters().size()==0);
		assertTrue(se0.getListParameters().length==0);
 
		Expr se1=new Expr("a=b",8);
		System.out.println("!"+se1.getSQL()+"|");
		System.out.println("!"+se1.getListParameterSQL()+"|");
		System.out.println("!"+se1.getNamedParameterSQL()+"|");
		assertTrue("a=b".equals(se1.getSQL()));
		assertTrue("a=b".equals(se1.getListParameterSQL()));
		assertTrue("a=b".equals(se1.getNamedParameterSQL()));
		assertTrue(se1.getNamedParameters().size()==0);
		assertTrue(se1.getListParameters().length==0);
		
		se0=se0.append("\n").append(" and b=?",90);
		System.out.println(se0.getSQL());
		System.out.println(se0.getListParameterSQL());
		System.out.println(se0.getNamedParameterSQL());
		assertTrue("and b= 90".equals(se0.getSQL()));
		assertTrue("and b= ?".equals(se0.getListParameterSQL()));
		assertTrue("and b= :PARAM_1".equals(se0.getNamedParameterSQL()));
		
		assertTrue(se0.getNamedParameters().size()==1);
		assertTrue(se0.getListParameters().length==1);
		
		assertTrue((Integer)se0.getListParameters()[0]==90);
		assertTrue((Integer)se0.getNamedParameters().get("PARAM_1")==90);
		
		Expr se=new Expr("A=?",DemoStatus.AA);
		System.out.println(se.getSQL());	
		System.out.println(se.getListParameterSQL());
		System.out.println(se.getNamedParameterSQL());
		assertTrue("A= 'AA'".equals(se.getSQL()));
		assertTrue("A= ?".equals(se.getListParameterSQL()));
		assertTrue("A= :PARAM_1".equals(se.getNamedParameterSQL()));
		
		assertTrue(se.getNamedParameters().size()==1);
		assertTrue(se.getListParameters().length==1);
		
		assertTrue(DemoStatus.AA.name().equals(se.getListParameters()[0]));
		assertTrue(DemoStatus.AA.name().equals(se.getNamedParameters().get("PARAM_1")));
	}
	
	@Test
	public  void se_test_jump() {
		System.err.println("======== se_test_jump =================================");
		//ignor comments
		Expr se=new Expr("a=? /* and b=?  */ and c=?",1,2);
		System.out.println(se.getSQL());
		assertTrue("a= 1 /* and b=?  */ and c= 2".equals(se.getSQL()));
		
		//ignor comments
		se=new Expr("a=? -- and b=?  \n and c=?",1,2);
		System.out.println(se.getSQL());
		assertTrue("a= 1 -- and b=?  \n and c= 2".equals(se.getSQL()));
		
		//ignor chars
		se=new Expr("a=?  and b='?' and c=?",1,2);
		System.out.println(se.getSQL());
		assertTrue("a= 1 and b='?' and c= 2".equals(se.getSQL()));
	}
	
	@Test
	public  void se_test_misc() {
		
		System.err.println("======== se_test_misc =================================");
		
		Expr se0=new Expr("#aaaa\nb=?\n#cccc\n",8);
		System.out.println(se0.getSQL());
		assertTrue("#aaaa\nb= 8 #cccc".equals(se0.getSQL()));
		
		
		//简单SE参数
		Expr se1=new Expr("name = ?","leefj");
		System.out.println(se1.getSQL());
		System.out.println(se1.getListParameterSQL());
		System.out.println(se1.getNamedParameterSQL());
		
		//直接输出
		assertTrue("name = 'leefj'".equals(se1.toString()));
		assertTrue("name = 'leefj'".equals(se1+""));
		
		//校验
		assertTrue("name = 'leefj'".equals(se1.getSQL()));
		assertTrue("name = ?".equals(se1.getListParameterSQL()));
		assertTrue("name = :PARAM_1".equals(se1.getNamedParameterSQL()));
		
		MapParamBuilder ps=new MapParamBuilder();
		ps.set("age_num", 27);
		Expr se2=new Expr("age > :age_num",ps.map());
//		//打印 : age > 27
		System.out.println(se2.getSQL());
		System.out.println(se2.getListParameterSQL());
		System.out.println(se2.getNamedParameterSQL());
		
		assertTrue("age > 27".equals(se2.getSQL()));
		assertTrue("age > ?".equals(se2.getListParameterSQL()));
		assertTrue("age > :PARAM_1".equals(se2.getNamedParameterSQL()));
		
//		//合并两个表达式
		Expr se3= se1.append("and").append(se2);
		System.out.println(se3.getSQL());
		System.out.println(se3.getListParameterSQL());
		System.out.println(se3.getNamedParameterSQL());
		
		assertTrue("name = 'leefj' and age > 27".equals(se3.getSQL()));
		assertTrue("name = ? and age > ?".equals(se3.getListParameterSQL()));
		assertTrue("name = :PARAM_1 and age > :PARAM_2".equals(se3.getNamedParameterSQL()));
	
	 
		Object[] arrayParams = null;
		try {
			arrayParams = se3.getListParameters();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(JSONObject.toJSONString(arrayParams)); 		
		assertTrue("[\"leefj\",27]".equals(JSONObject.toJSONString(arrayParams)));
 
		//获得参数Map
		Map<String,Object> mapParams=se3.getNamedParameters();
		//打印 {"PARAM_1":"leefj","PARAM_2":27}
		System.out.println(JSONObject.toJSONString(mapParams)); 
		assertTrue("{\"PARAM_1\":\"leefj\",\"PARAM_2\":27}".equals(JSONObject.toJSONString(mapParams)));

	}
	
	@Test
	public void test_long_statement()
	{
		String code="code",path="111";
		
		String[] sqls= {
				"INSERT INTO  pms_org_unit_user(unit_code, unit_name,   user_badge, user_id, user_name,user_account)",
				"select distinct ? unit_code,o.unit_name,  u.badge,u.id,u.name,u.account from pms_org_users u",
				"LEFT JOIN pms_org_station s ON s.station_code = u.marjor_station_code",
				"LEFT JOIN pms_org_unit o ON  o.unit_code = s.unit_code",
				"where o.is_enabled = 'T' and u.account_enabled = 'T' and s.is_enabled = 'T' and u.badge is not null and u.account is not null",
				"and (o.unit_code=? or o.path like ?)"
		};
		Expr se=new Expr(SQL.joinSQLs(sqls),code,code,path+"/%");
		System.out.println(se.getNamedParameterSQL());
		System.out.println(se.getNamedParameters());
		
		assertTrue(("INSERT INTO  pms_org_unit_user(unit_code, unit_name,   user_badge, user_id, user_name,user_account) \n" + 
				"select distinct :PARAM_1 unit_code,o.unit_name,  u.badge,u.id,u.name,u.account from pms_org_users u \n" + 
				"LEFT JOIN pms_org_station s ON s.station_code = u.marjor_station_code \n" + 
				"LEFT JOIN pms_org_unit o ON  o.unit_code = s.unit_code \n" +
				"where o.is_enabled = 'T' and u.account_enabled = 'T' and s.is_enabled = 'T' and u.badge is not null and u.account is not null \n" + 
				"and (o.unit_code= :PARAM_2 or o.path like :PARAM_3 )").equals(se.getNamedParameterSQL()));
		
		assertTrue(se.getNamedParameters().size()==3);
		
	}
	

}
