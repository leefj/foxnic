package com.github.foxnic.dao.queue;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.reflect.EnumUtil;

/**
 * 审批结果
 * */
public enum QueueStatus implements CodeTextEnum {

	waiting("待处理"),
	queue("排队中"),
	consuming("处理中"),
	consumed("已处理"),
	failure("处理失败");

	private String text;
	private QueueStatus(String text)  {
		this.text=text;
	}

	public String code() {
		return this.name();
	}

	public String text() {
		return text;
	}

	public static QueueStatus parseByCode(String code) {
		return (QueueStatus) EnumUtil.parseByCode(QueueStatus.values(),code);
	}

}
