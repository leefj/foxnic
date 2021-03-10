package com.github.foxnic.springboot.api.error;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class CommonError {

	public static final String SUCCESS_TEXT = "操作成功";
	public static final String FALIURE_TEXT = "操作失败";
	
	// 构建错误描述信息
	@PostConstruct
	private void init() {
		try {
			
			new ErrorDesc(SUCCESS, SUCCESS_TEXT);
			new ErrorDesc(FALIURE, FALIURE_TEXT);
			new ErrorDesc(INVALID_PARAM, "参数不合法");
			new ErrorDesc(INVALID_PARAM_FORMAT, "参数格式不合法");
			new ErrorDesc(INVALID_TOKEN, "鉴权失败,未传入token或使用的参数不正确");
			new ErrorDesc(EXPIRED_TOKEN, "token不在有效期内：当前时间在exp参数值之后或在nbf参数值之前");
			new ErrorDesc(INVALID_TOKEN_FORMAT, "token解码失败");
			new ErrorDesc(INVALID_TOKEN_SIGNATURE, "token签名校验失败");
			new ErrorDesc(ILLEGAL_SUBMIT_RATE, "请求太快");
			new ErrorDesc(FORBIDDEN_FUNCTION, "功能禁用");
			new ErrorDesc(INVALID_PARAM_VALUE, "参数值不合法");
			new ErrorDesc(INVALID_NETWORK, "网络连接异常");
			new ErrorDesc(INVALID_SERVER, "服务器不可用");
			new ErrorDesc(INVALID_NETWORK_OR_SERVER, "网络或服务器不可用");
			new ErrorDesc(INVALID_SERVER_APP_KEY, "AppKey无效");
			new ErrorDesc(INVALID_SERVER_APP_SECURE, "AppSecure无效");
			new ErrorDesc(INVALID_CAPTCHA, "验证码不合法");
			new ErrorDesc(EXPIRED_CAPTCHA, "验证码已过期");
			new ErrorDesc(WRONG_NAME_PWD, "错误的key和密钥组合");
			new ErrorDesc(BLOCKED_USER, "客户系统被禁用,请联系管理员");
			new ErrorDesc(INVALID_PASSWORD, "账户与密码不匹配");
			new ErrorDesc(INVALID_PASSWORD_STRENGTH, "密码强度不符合要求");
			
			new ErrorDesc(REQUIRE_KEY_CLAIM, "token的Payload部分缺少“key claim” 信息");
			new ErrorDesc(INVALID_SIGN_ALGORITHM, "无效/不支持的签名算法");
			new ErrorDesc(INVALID_KEY_CLAIM, "token的Payload中“key claim” 值无效");
			
			
			new ErrorDesc(INVALID_DB_TABLE, "数据表不存在");
			new ErrorDesc(INVALID_DB_FIELD, "字段不存在");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 设定前缀
	private static final String PREFIX = "";
	// 定义通用错误
	/**
	 * 操作成功
	 */
	public static final String SUCCESS = PREFIX + "00";
	/**
	 * 操作失败
	 */
	public static final String FALIURE = PREFIX + "01";

	/**
	 * 参数不合法
	 */
	public static final String INVALID_PARAM = PREFIX + "02";

	/**
	 * 参数不合法
	 */
	public static final String INVALID_PARAM_FORMAT = PREFIX + "03";

	/**
	 * token缺失
	 */
	public static final String INVALID_TOKEN = PREFIX + "04";

	/**
	 * token格式不合法
	 */
	public static final String EXPIRED_TOKEN = PREFIX + "05";

	/**
	 * token签名验证失败
	 */
	public static final String INVALID_TOKEN_FORMAT = PREFIX + "06";

	/**
	 * 请求太快
	 */
	public static final String ILLEGAL_SUBMIT_RATE = PREFIX + "07";

	/**
	 * 功能禁用
	 */
	public static final String FORBIDDEN_FUNCTION = PREFIX + "08";

	/**
	 * 参数值不合法
	 */
	public static final String INVALID_PARAM_VALUE = PREFIX + "09";
	/**
	 * 网络连接异常
	 */
	public static final String INVALID_NETWORK = PREFIX + "10";
	/**
	 * 服务器不可用
	 */
	public static final String INVALID_SERVER = PREFIX + "11";
	/**
	 * 网络或服务器不可用
	 */
	public static final String INVALID_NETWORK_OR_SERVER = PREFIX + "12";
	/**
	 * appKey无效
	 */
	public static final String INVALID_SERVER_APP_KEY = PREFIX + "13";
	/**
	 * AppSecure 无效
	 */
	public static final String INVALID_SERVER_APP_SECURE = PREFIX + "14";

	/**
	 * 验证码不合法
	 */
	public static final String INVALID_CAPTCHA = PREFIX + "15";

	/**
	 * 验证码已过期
	 */
	public static final String EXPIRED_CAPTCHA = PREFIX + "16";
	/**
	 * 错误的key和密钥组合
	 */
	public static final String WRONG_NAME_PWD = PREFIX + "17";
	/**
	 * 客户系统被禁用,请联系管理员
	 */
	public static final String BLOCKED_USER = PREFIX + "18";
	/**
	 * 账户与密码不匹配
	 */
	public static final String INVALID_PASSWORD = PREFIX + "19";

	/**
	 * 签名验证失败
	 */
	public static final String INVALID_TOKEN_SIGNATURE = PREFIX + "20";
	/**
	 * 数据表不存在
	 * */
	public static final String INVALID_DB_TABLE = PREFIX + "21";
	
	/**
	 * 字段不存在
	 * */
	public static final String INVALID_DB_FIELD = PREFIX + "22";
	
	/**
	 * 密码强度不符合要求
	 * */
	public static final String INVALID_PASSWORD_STRENGTH = PREFIX + "23";
	
	/**
	 * token的Payload部分缺少“key claim” 信息
	 * */
	public static final String REQUIRE_KEY_CLAIM = PREFIX + "24";
	
	
	/**
	 *  无效/不支持的签名算法
	 * */
	public static final String INVALID_SIGN_ALGORITHM = PREFIX + "25";
	
	/**
	 *  token的Payload中“key claim” 值无效 
	 * */
	public static final String INVALID_KEY_CLAIM  = PREFIX + "26";
	
	
	

}
