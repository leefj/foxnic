package com.github.foxnic.dao.base.jpa;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Date;

import javax.persistence.Table;

import com.github.foxnic.dao.base.pojo.BasePojo;

@Table(name="test_news")
public class NewsJPA extends BasePojo {

	private Long id;
	
	
	private String code;

	private String title;

	private Date publishDay;

	private Timestamp enterTime;
	
	private String newsId;

	private Integer read_times;

	private Double price;
	
	private LocalTime alertTime;
	
	public LocalTime getAlertTime() {
		return alertTime;
	}

	public void setAlertTime(LocalTime alertTime) {
		this.alertTime = alertTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getPublishDay() {
		return publishDay;
	}

	public void setPublishDay(Date publishDay) {
		this.publishDay = publishDay;
	}

	public Timestamp getEnterTime() {
		return enterTime;
	}

	public void setEnterTime(Timestamp enterTime) {
		this.enterTime = enterTime;
	}

	public String getNewsId() {
		return newsId;
	}

	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}

	public Integer getRead_times() {
		return read_times;
	}

	public void setRead_times(Integer read_times) {
		this.read_times = read_times;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}
