package com.github.foxnic.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.sql.expr.ConditionExprAdvanceTest;
import com.github.foxnic.sql.expr.ConditionExprTest;
import com.github.foxnic.sql.expr.DeleteTest;
import com.github.foxnic.sql.expr.ExprTest;
import com.github.foxnic.sql.expr.InsertTest;
import com.github.foxnic.sql.expr.SQLParserUtilTest;
import com.github.foxnic.sql.expr.SQLTplTest;
import com.github.foxnic.sql.expr.StatementUtilTest;
import com.github.foxnic.sql.expr.UpdateTest;
import com.github.foxnic.sql.parser.ParserTest;

@RunWith(Suite.class)
@SuiteClasses({
	SQLParserUtilTest.class,StatementUtilTest.class,ExprTest.class,ConditionExprTest.class,DeleteTest.class,
	InsertTest.class,SQLTplTest.class,UpdateTest.class,ConditionExprAdvanceTest.class,
	ParserTest.class
})
public class SQLTestSuite {

	@BeforeClass
	public static void begin() {
		
	}

	@AfterClass
	public static void end() {

	}
}
