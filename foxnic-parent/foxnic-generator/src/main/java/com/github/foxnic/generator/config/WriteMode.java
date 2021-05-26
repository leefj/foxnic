package com.github.foxnic.generator.config;

public enum WriteMode {
		/**
		 * 如果文件已经存在，在边上生成一个 .code 文件
		 * */
		WRITE_TEMP_FILE,
		/**
		 * 如果文件已经存在，直接覆盖原始文件;不存在就创建
		 * */
		COVER_EXISTS_FILE,
		/**
		 * 如果文件不存在则创建，如果已存在就不处理
		 * */
		CREATE_IF_NOT_EXISTS,
		/**
		 * 完全忽略，不做生成任何代码
		 * */
		IGNORE;
	}