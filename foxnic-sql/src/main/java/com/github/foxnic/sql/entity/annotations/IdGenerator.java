package com.github.foxnic.sql.entity.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public  @interface IdGenerator {
	
	public enum GeneratorType {
		/**
		 * 不指定生成方式，需要手动设置
		 * */
		NONE,
		/**
		 * 使用 SNOWFLAKE 算法生成，支持集群模式
		 * */
		SNOWFLAKE,
		/**
		 * 使用短的UUID，在UUID基础上取16位MD5
		 * */
		SUID,
		/**
		 * 使用UUID
		 * */
		UUID,
		/**
		 * 使用自增，需数据库存储过程支持
		 * */
		AI,
		/**
		 * 使用按日期自增，如20200118002,20200118003，需数据库存储过程支持
		 * */
		DAI,
		/**
		 * 按周自增，格式为年份+周序号
		 * */
		WAI_1,
		/**
		 * 按周自增，格式为年份+周序号
		 * */
		WAI_2,
		/**
		 * 按月自增，格式为年份+月份+序号
		 * */
		MAI,
		/**
		 * 按周年增，格式为年份+序号
		 * */
		YAI;
	}
 
	
	String name() default "";
	GeneratorType type() default GeneratorType.NONE;
	int length() default 6;
	int fetchSize() default 4;
	
 
}
