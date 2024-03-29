package com.github.foxnic.api.error;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CommonError extends ErrorDefinition {

	public static final String SUCCESS_TEXT = "操作成功";
	public static final String FALIURE_TEXT = "操作失败";

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
	public static final String PARAM_INVALID = PREFIX + "02";

	/**
	 * 参数不合法
	 */
	public static final String PARAM_INVALID_FORMAT = PREFIX + "03";

	/**
	 * token缺失
	 */
	public static final String TOKEN_INVALID = PREFIX + "04";

	/**
	 * token格式不合法
	 */
	public static final String TOKEN_EXPIRED = PREFIX + "05";

	/**
	 * token签名验证失败
	 */
	public static final String TOKEN_FORMAT_INVALID = PREFIX + "06";

	/**
	 * 请求太快
	 */
	public static final String SUBMIT_RATE_ILLEGAL = PREFIX + "07";

	/**
	 * 功能禁用
	 */
	public static final String FUNCTION_FORBIDDEN = PREFIX + "08";

	/**
	 * 参数值不合法
	 */
	public static final String PARAM_VALUE_INVALID = PREFIX + "09";
	/**
	 * 网络连接异常
	 */
	public static final String NETWORK_INVALID = PREFIX + "10";
	/**
	 * 服务器不可用
	 */
	public static final String SERVER_INVALID = PREFIX + "11";
	/**
	 * 网络或服务器不可用
	 */
	public static final String NETWORK_OR_SERVER_INVALID = PREFIX + "12";
	/**
	 * appKey无效
	 */
	public static final String SERVER_APP_KEY_INVALID = PREFIX + "13";
	/**
	 * AppSecure 无效
	 */
	public static final String SERVER_APP_SECURE_INVALID = PREFIX + "14";

	/**
	 * 验证码不合法
	 */
	public static final String CAPTCHA_INVALID = PREFIX + "15";

	/**
	 * 验证码已过期
	 */
	public static final String CAPTCHA_EXPIRED = PREFIX + "16";
	/**
	 * 账户或密码错误
	 */
	public static final String NAME_PWD_WRONG = PREFIX + "17";
	/**
	 * 客户系统被禁用,请联系管理员
	 */
	public static final String USER_BLOCKED = PREFIX + "18";
	/**
	 * 无效密码
	 */
	public static final String PASSWORD_INVALID = PREFIX + "19";

	/**
	 * 签名验证失败
	 */
	public static final String TOKEN_SIGNATURE_INVALID = PREFIX + "20";
	/**
	 * 数据表不存在
	 * */
	public static final String DB_TABLE_INVALID = PREFIX + "21";

	/**
	 * 字段不存在
	 * */
	public static final String DB_FIELD_INVALID = PREFIX + "22";

	/**
	 * 密码强度不符合要求
	 * */
	public static final String PWD_STRENGTH_INVALID = PREFIX + "23";

	/**
	 * token的Payload部分缺少“key claim” 信息
	 * */
	public static final String KEY_CLAIM_REQUIRE = PREFIX + "24";


	/**
	 *  无效/不支持的签名算法
	 * */
	public static final String SIGN_ALGORITHM_INVALID = PREFIX + "25";

	/**
	 *  token的Payload中“key claim” 值无效
	 * */
	public static final String KEY_CLAIM_INVALID = PREFIX + "26";


	/**
	 *  验证码不匹配
	 * */
	public static final String CAPTCHA_NOT_MATCH  = PREFIX + "27";

	/**
	 *  无效的文件
	 * */
	public static final String FILE_INVALID  = PREFIX + "28";

	/**
	 *  文件类型不允许上传
	 * */
	public static final String FILE_TYPE_NOT_ALLOWED  = PREFIX + "29";

	/**
	 *  文件类型不允许上传
	 * */
	public static final String FILE_NOT_EXISTS  = PREFIX + "30";

	/**
	 * 会话不可用或未登录
	 */
	public static final String SESSION_INVALID = PREFIX + "31";

	/**
	 * 权限不足
	 */
	public static final String PERMISSION_REQUIRED = PREFIX + "32";


	/**
	 *  数据重复
	 * */
	public static final String DATA_REPETITION  = PREFIX + "33";

	/**
	 *  数据不存在
	 * */
	public static final String DATA_NOT_EXISTS = PREFIX + "34";

	/**
	 * 不允许删除
	 * */
	public static final String DELETE_NOT_ALLOWED = PREFIX + "35";


	/**
	 *  证书已过期
	 * */
	public static final String CREDENTIALS_EXPIRED  = PREFIX + "36";

	/**
	 * 账户已过期
	 */
	public static final String USER_EXPIRED = PREFIX + "37";

	/**
	 * 账户已禁用
	 */
	public static final String USER_DISABLED = PREFIX + "38";

	/**
	 * 账户未注册
	 */
	public static final String USER_NOT_EXISTS = PREFIX + "39";


	/**
	 * 参数不允许为空
	 */
	public static final String PARAM_IS_REQUIRED = PREFIX + "40";

	/**
	 * 参数值不允许在指定的列表内
	 */
	public static final String VALUE_CAN_NOT_IN_LIST = PREFIX + "41";

	/**
	 * 参数值必须在指定的列表内
	 */
	public static final String VALUE_MUST_IN_LIST = PREFIX + "42";

	/**
	 * 参数要求为空
	 */
	public static final String PARAM_IS_NOT_REQUIRED = PREFIX + "43";


	/**
	 * 参数被拒绝
	 */
	public static final String PARAM_IS_REJECTED = PREFIX + "44";

	/**
	 *  执行异常
	 * */
	public static final String EXCEPTOPN  = PREFIX + "99";

	@PostConstruct
	public void init() {
		try {

			new ErrorDesc(SUCCESS, SUCCESS_TEXT);
			new ErrorDesc(FALIURE, FALIURE_TEXT);
			new ErrorDesc(PARAM_INVALID, "参数不合法");
			new ErrorDesc(PARAM_INVALID_FORMAT, "参数格式不合法");
			new ErrorDesc(TOKEN_INVALID, "鉴权失败,未传入token或使用的参数不正确");
			new ErrorDesc(TOKEN_EXPIRED, "token不在有效期内：当前时间在exp参数值之后或在nbf参数值之前");
			new ErrorDesc(TOKEN_FORMAT_INVALID, "token解码失败");
			new ErrorDesc(TOKEN_SIGNATURE_INVALID, "token签名校验失败");
			new ErrorDesc(SUBMIT_RATE_ILLEGAL, "请求太快");
			new ErrorDesc(FUNCTION_FORBIDDEN, "功能禁用");
			new ErrorDesc(PARAM_VALUE_INVALID, "参数值不合法");
			new ErrorDesc(NETWORK_INVALID, "网络连接异常");
			new ErrorDesc(SERVER_INVALID, "服务器不可用");
			new ErrorDesc(NETWORK_OR_SERVER_INVALID, "网络或服务器不可用");
			new ErrorDesc(SERVER_APP_KEY_INVALID, "AppKey无效");
			new ErrorDesc(SERVER_APP_SECURE_INVALID, "AppSecure无效");
			new ErrorDesc(CAPTCHA_INVALID, "验证码不合法");
			new ErrorDesc(CAPTCHA_EXPIRED, "验证码已过期");
			new ErrorDesc(CAPTCHA_NOT_MATCH, "验证码不匹配");
			new ErrorDesc(NAME_PWD_WRONG, "账户密码错误");
			new ErrorDesc(SESSION_INVALID, "会话不可用或未登录");
			new ErrorDesc(PERMISSION_REQUIRED, "权限不足");
			new ErrorDesc(CREDENTIALS_EXPIRED, "证书已过期");

			new ErrorDesc(USER_NOT_EXISTS, "账户未注册");
			new ErrorDesc(USER_BLOCKED, "账户已锁定");
			new ErrorDesc(USER_EXPIRED, "账户已过期");
			new ErrorDesc(USER_DISABLED, "账户已禁用");
			new ErrorDesc(PASSWORD_INVALID, "账户与密码不匹配");
			new ErrorDesc(PWD_STRENGTH_INVALID, "密码强度不符合要求");

			new ErrorDesc(KEY_CLAIM_REQUIRE, "token的Payload部分缺少“key claim” 信息");
			new ErrorDesc(SIGN_ALGORITHM_INVALID, "无效/不支持的签名算法");
			new ErrorDesc(KEY_CLAIM_INVALID, "token的Payload中“key claim” 值无效");


			new ErrorDesc(DB_TABLE_INVALID, "数据表不存在");
			new ErrorDesc(DB_FIELD_INVALID, "字段不存在");

			new ErrorDesc(FILE_INVALID, "无效的文件");
			new ErrorDesc(FILE_NOT_EXISTS, "文件不存在");
			new ErrorDesc(FILE_TYPE_NOT_ALLOWED, "文件类型不允许");


			new ErrorDesc(DATA_REPETITION, "数据重复");
			new ErrorDesc(DATA_NOT_EXISTS, "数据不存在");

			new ErrorDesc(DELETE_NOT_ALLOWED, "不允许删除");

			new ErrorDesc(PARAM_IS_REQUIRED, "参数不允许为空");
			new ErrorDesc(PARAM_IS_NOT_REQUIRED, "参数要求为空");
			new ErrorDesc(PARAM_IS_REJECTED, "参数不允许");

			new ErrorDesc(VALUE_CAN_NOT_IN_LIST, "值不允许在指定范围内");
			new ErrorDesc(VALUE_MUST_IN_LIST, "值必须在指定范围内");


			//
			new ErrorDesc(EXCEPTOPN, "执行异常");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
