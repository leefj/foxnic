package com.demo.configs;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spec.DAOBuilder;

@Configuration
public class DBConfigs {
	
	public static final String PRIMARY_DATASOURCE_CONFIG_KEY = "spring.datasource.druid";
	public static final String PRIMARY_DATA_SOURCE_NAME = "primaryDataSource";
	public static final String PRIMARY_DAO = "primaryDAO";
	
	
	@Bean(name = PRIMARY_DATA_SOURCE_NAME)
	@ConfigurationProperties(PRIMARY_DATASOURCE_CONFIG_KEY)
	public DruidDataSource primaryDataSource() {
		return DruidDataSourceBuilder.create().build();
	}
	
	@Bean
	@Primary
	public DataSourceTransactionManager primaryTransactionManager(
			@Qualifier(PRIMARY_DATA_SOURCE_NAME) DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
	
	@Bean(PRIMARY_DAO)
	@Primary
	public DAO primaryDAO (
			@Qualifier(PRIMARY_DATA_SOURCE_NAME) DataSource dataSource) {
		try {
			DAO dao= (new DAOBuilder().datasource(dataSource)).build();
			dao.setPrintSQL(true);
			dao.setPrintSQLSimple(true);
			return dao;
		} catch (Exception e) {
			Logger.error("创建DAO失败",e);
			return null;
		}
	}

}
