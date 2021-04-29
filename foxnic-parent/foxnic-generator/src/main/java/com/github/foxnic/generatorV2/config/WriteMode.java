package com.github.foxnic.generatorV2.config;

public enum WriteMode {
		/**
		 * 如果文件已经存在，在边上生成一个 .code 文件
		 * */
		WRITE_TEMP_FILE,
		
		/**
		 * 如果文件已经存在，直接覆盖原始文件
		 * */
		WRITE_DIRECT,
		/**
		 * 如果文件已经存在，直接覆盖原始文件
		 * */
		DO_NOTHING;
	}