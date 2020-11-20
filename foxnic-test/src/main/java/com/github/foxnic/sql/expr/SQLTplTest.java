package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
 

public class SQLTplTest {
	
	private static String[] SQL_1= {
			"SELECT",
			"qs.project_id projectId,",
			"pp.`name` projectName,",
			"pp.`busi_code` busiCode,",
			"pus.`name` unit,",
			"su.user_id,",
			"su.user_name projectMajor,",
			"pda.dict_label itType,",
			"pdb.dict_label type,",
			"SUM(IF (sdd.dict_label = '1-80',qs.score , '')) january,",
			"SUM(IF (sdd.dict_label = '2-85',qs.score , '')) february,",
			"SUM(IF (sdd.dict_label = '3-90',qs.score , '')) march,",
			"SUM(IF (sdd.dict_label = '6-95',qs.score , '')) june",
			"FROM",
			"qs_survey qs",
			"LEFT JOIN pms_project pp ON qs.project_id = pp.id",
			"LEFT JOIN pms_unit_setup pus ON pp.`busi_code` = pus.code",
			"LEFT JOIN (SELECT dict_code,dict_label,dict_value,dict_type FROM sys_dict_data WHERE dict_type ='PMS_IT_SYSTEM_TYPE')",
			"pda ON pp.it_type = pda.dict_value",
			"LEFT JOIN pms_manage_type pmt ON pp.manage_type = pmt.id",
			"LEFT JOIN (SELECT dict_code,dict_label,dict_value,dict_type FROM sys_dict_data WHERE dict_type ='PMS_PROJECT_SCALE')",
			"pdb ON pmt.project_scale_id = pdb.dict_value",
			"LEFT JOIN (SELECT dict_code,dict_label,dict_value,dict_type FROM sys_dict_data WHERE dict_type ='PMS_SCORE_RANGE')",
			"sdd ON qs.activity_code = sdd.dict_value",
			"LEFT JOIN sys_user su ON pp.director_id = su.user_id",
			"LEFT JOIN pms_project_phase ppp ON qs.activity_code = ppp.`code`",
			"WHERE",
			"qs.`status` = 'DONE'",
			"#{WHERES_INFO}",
			"AND qs.score IS NOT NULL and a='#{WHERES_INFO}'",
			"AND qs.activity_code in (SELECT dict_value FROM sys_dict_data WHERE dict_type ='PMS_SCORE_RANGE')",
			"GROUP BY",
			"qs.project_id ,",
			"pp.`name` ,",
			"pp.`busi_code` ,",
			"pus.`name` ,",
			"su.user_name ,",
			"pda.dict_label ,",
			"pdb.dict_label ,",
			"qs.score ,",
			"qs.paper_id",
			"order by a asc , b desc"
	};
	
	private static String[] SQL_2= {
		"SELECT",
		"su.user_name userName,",
		"qsi.use_badge userBadge,",
		"sd.dept_name deptName,",
		"qsi.is_in_project isInProject,",
		"qq.question,qsa.score,qsf.title,qsf.detail",
		"from  qs_survey_answer qsa",
		"LEFT JOIN qs_survey_issue qsi ON qsa.issue_id = qsi.id",
		"LEFT JOIN qs_survey qs ON qsi.survey_id = qs.id",
		"LEFT JOIN sys_user su ON qsi.use_id = su.user_id",
		"LEFT JOIN sys_dept sd ON qsi.unit_code = sd.dept_code",
		"LEFT JOIN qs_question qq ON qsa.question_id = qq.id",
		"INNER JOIN qs_survey_feedback qsf ON qq.id = qsf.question_id",
		"WHERE qsa.issue_id =qsf.issue_id",
		"#{PH}"
	};

	private static String[] SQL_3= {
		"select su.user_name userName,",
		"qsi.use_badge userBadge,sd.dept_name deptName,",
		"qsi.is_in_project isInProject,qsi.score,",
		"#{QUESTION_LIST}",
		"from  qs_survey_answer qsa",
		"LEFT JOIN qs_survey_issue qsi ON qsa.issue_id = qsi.id",
		"LEFT JOIN qs_survey qs ON qsi.survey_id = qs.id",
		"LEFT JOIN sys_user su ON qsi.use_id = su.user_id",
		"LEFT JOIN sys_dept sd ON qsi.unit_code = sd.dept_code",
		"LEFT JOIN qs_question qq ON qsa.question_id = qq.id",
		"WHERE qs.project_id = ? AND qs.activity_code=?",
		"GROUP BY su.user_name,qsi.use_badge,sd.dept_name,qsi.is_in_project,qsi.score"
	};
	
	
	@Test
	public void test4() throws IOException {
 
		System.err.println("========== test4 ============");
		
		String content=SQL.joinSQLs(SQL_3);
		SQLTpl tpl=new SQLTpl(content,6,19);
		tpl.setPlaceHolder("QUESTION_LIST", new Expr("question like ?","Q-909%"));
		System.out.println(tpl.getSQL());
		
		assertTrue(tpl.getSQL().contains("question like 'Q-909%'"));
		
	}
	
	@Test
	public void test3() throws IOException {
		System.err.println("========== test3 ============");
		String content=SQL.joinSQLs(SQL_2);
		SQLTpl tpl=new SQLTpl(content);
		tpl.setPlaceHolder("PH", new ConditionExpr("question like ?","Q-909%"));
		System.out.println(tpl.getSQL());
		assertTrue(tpl.getSQL().contains("question like 'Q-909%'"));
	}
	
	@Test
	public void test2() throws IOException {
		System.err.println("========== test2 ============");
		String content=SQL.joinSQLs(SQL_1);
		SQLTpl tpl=new SQLTpl(content);
		tpl.setPlaceHolder("WHERES_INFO", new ConditionExpr("question like ?","Q-909%"));
		System.out.println(tpl.getSQL());
		assertTrue(tpl.getSQL().contains("question like 'Q-909%'"));
		assertTrue(tpl.getSQL().contains("a='#{WHERES_INFO}'"));
		
	}
	
	@Test
	public void test1() {
		System.err.println("========== test1 ============");
		Map<String,Object> ps=new HashMap<>();
		ps.put("KS", "KKS");
		long t=System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			SQLTpl tpl=new SQLTpl("select ? #{AAAA},'#{AX}',\"#{BX}\",`#{CX}`,:KS from sys_user T1 a , (#{TB2}) b where a.id=b.id and name=?",ps,1,"lfj")  ;
			tpl.setPlaceHolder("AAAA","ifnull(f1,?)",90);
			tpl.setPlaceHolder("TB2","select * from tableB where id=?",1930);
			
			String sql=tpl.getSQL();
			
			assertTrue("select 1 ifnull(f1, 90 ) '#{AX}',\"#{BX}\",`#{CX}`, 'KKS' from sys_user T1 a , ( select * from tableB where id= 1930 b where a.id=b.id and name= 'lfj'".equals(sql));
			
			System.out.println(sql);
		}
		long t2=System.currentTimeMillis();
		
		System.out.println("tm ============== "+(t2-t));
		
	}
}
