package com.github.foxnic.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.sql.expr.ConditionExprAdvanceTest;
import com.github.foxnic.sql.expr.ConditionExprAndIfTest;
import com.github.foxnic.sql.expr.ConditionExprTest;
import com.github.foxnic.sql.expr.DeleteTest;
import com.github.foxnic.sql.expr.ExprTest;
import com.github.foxnic.sql.expr.InsertTest;
import com.github.foxnic.sql.expr.NameConvertorTest;
import com.github.foxnic.sql.expr.SQLTplTest;
import com.github.foxnic.sql.expr.SelectTest;
import com.github.foxnic.sql.expr.SetterTest;
import com.github.foxnic.sql.expr.UpdateTest;
import com.github.foxnic.sql.parser.ParserTest;
import com.github.foxnic.sql.parser.SQLParserUtilTest;
import com.github.foxnic.sql.parser.StatementUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
	//expr  部分
	ConditionExprAdvanceTest.class,ConditionExprAndIfTest.class,ConditionExprTest.class,
	DeleteTest.class,ExprTest.class,InsertTest.class,
	SQLParserUtilTest.class,StatementUtilTest.class,
	NameConvertorTest.class,SelectTest.class,SetterTest.class,
	SQLTplTest.class,UpdateTest.class,
	//parser 部分
	ParserTest.class,SQLParserUtilTest.class,StatementUtilTest.class
})
public class SQLTestSuite {

	@BeforeClass
	public static void begin() {
		
	}

	@AfterClass
	public static void end() {

	}
}
