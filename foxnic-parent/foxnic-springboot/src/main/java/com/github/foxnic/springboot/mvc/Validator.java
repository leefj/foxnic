package com.github.foxnic.springboot.mvc;

import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import org.apache.poi.ss.formula.functions.T;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 数据校验器
 * */
public class Validator {



    private static interface  SingleValueValidatorFunction<T> {
        Result validate(T value);
    }

    public static class ValueWrapper<T> {
        private Validator validator;
        private String subject;
        private T value;
        private ValueWrapper (Validator validator,String subject,T value) {
            this.validator=validator;
            this.subject=subject;
            this.value=value;
        }
        /**
         * 返回上层校验器
         * */
        public Validator validator() {
            return this.validator;
        }

        /**
         * 必填项，非 null 或 非空白字符串 或 非空集合
         * */
        public ValueWrapper<T> require(String message) {
            Result result=null;
            if(value==null) {
                result = ErrorDesc.failure(CommonError.PARAM_IS_REQUIRED).subject(subject);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            } else {
                boolean failure =false;
                if ((value instanceof CharSequence) && StringUtil.isBlank(value)) {
                   failure =true;
                } else if ((value instanceof Collection) && ((Collection)value).isEmpty()) {
                    failure =true;
                }
                if(failure) {
                    result = ErrorDesc.failure(CommonError.PARAM_IS_REQUIRED).subject(subject);
                    if(StringUtil.hasContent(message)) {
                        result.message(message);
                    }
                    this.validator.errors.add(result);
                }

            }
            return this;
        }

        /**
         * 必填项，非 null 或 非空白字符串
         * */
        public ValueWrapper<T> require() {
            return require(null);
        }


        /**
         *  要求 null 或 空串 或 空的集合
         * */
        public ValueWrapper<T> requireEmpty(String message) {
            Result result=null;
            if(value!=null) {
                boolean failure =true;
                if ((value instanceof CharSequence) && StringUtil.isBlank(value)) {
                    failure =false;
                } else if ((value instanceof Collection) && ((Collection)value).isEmpty()) {
                    failure =false;
                }
                if(failure) {
                    result = ErrorDesc.failure(CommonError.PARAM_IS_NOT_REQUIRED).subject(subject);
                    if(StringUtil.hasContent(message)) {
                        result.message(message);
                    }
                    this.validator.errors.add(result);
                }
            }
            return this;
        }

        /**
         *  要求 null 或 空串 或空的集合
         * */
        public ValueWrapper<T> requireEmpty() {
            return  requireEmpty(null);
        }

        /**
         *  参数不被允许，必须为 null
         * */
        public ValueWrapper<T> reject(String message) {
            Result result=null;
            if(this.value!=null) {
                result=ErrorDesc.failure(CommonError.PARAM_IS_REJECTED).subject(subject);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         *  参数不被允许，必须为 null
         * */
        public ValueWrapper<T> reject() {
            return  reject(null);
        }

        /**
         * 要求 null 值
         * */
        public  ValueWrapper<T> requireNull(String message) {
            Result result=null;
            if(this.value!=null) {
                result=ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求 null 值
         * */
        public  ValueWrapper<T> requireNull() {
            return requireNull(null);
        }


        /**
         * 要求为指定的类型
         * */
        public  ValueWrapper<T> requireType(String message,Class type) {
            Result result=null;
            boolean isType = false;
            if(value==null) {
                isType = false;
            }
            else {
               isType=ReflectUtil.isSubType(type, value.getClass());
            }

            if(!isType) {
                result=ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不是要求的数据类型 "+type.getName());
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }

            return this;
        }

        /**
         * 要求为指定的类型
         * */
        public  ValueWrapper<T> requireType(Class type) {
            return requireType(null,type);
        }


        /**
         * 不允许为指定的类型
         * */
        public  ValueWrapper<T> requireNotType(String message,Class type) {
            Result result=null;
            boolean isType = false;
            if(value==null) {
                isType = false;
            }
            else {
                isType=ReflectUtil.isSubType(type, value.getClass());
            }

            if(isType) {
                result=ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不允许的数据类型 "+type.getName());
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }

            return this;
        }

        /**
         * 不允许为指定的类型
         * */
        public  ValueWrapper<T> requireNotType(Class type) {
            return requireNotType(null,type);
        }


        /**
         * 要求非 null 值
         * */
        public  ValueWrapper<T> requireNotNull(String message) {
            Result result=null;
            if(this.value==null) {
                result=ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }


        /**
         * 要求两个值相等
         * */
        public  ValueWrapper<T> requireEqual(String message,T value) {
            Result result=null;
            boolean equals=true;
            if(this.value==null || value == null)
            {
                equals = false;
            }
            else {
                equals=this.value.equals(value);
            }

            if(!equals) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("值要求等于 "+value);
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求两个值相等
         * */
        public  ValueWrapper<T> requireEqual(T value) {
            return requireEqual(null,value);
        }


        /**
         * 要求字符串包含
         * */
        public  ValueWrapper<T> requireContains(String message,T value) {
            Result result=null;
            if(value==null || (value!=null && !(value instanceof String))) {
                throw new IllegalArgumentException("仅支持字符串类型的比较");
            }
            boolean contains=false;
            if(this.value==null || value==null) {
                contains = false;
            } else   {
                contains=((String)this.value).contains((String)value);
            }
            if(!contains) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("要求包含字符串 "+value);
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求字符串包含
         * */
        public  ValueWrapper<T> requireContains(T value) {
            return requireContains(null,value);
        }



        /**
         * 不允许包含指定的字符串
         * */
        public  ValueWrapper<T> requireNotContains(String message,T value) {
            Result result=null;
            if(value==null || (value!=null && !(value instanceof String))) {
                throw new IllegalArgumentException("仅支持字符串类型的比较");
            }
            boolean contains=false;
            if(this.value==null || value==null) {
                contains = false;
            } else   {
                contains=((String)this.value).contains((String)value);
            }
            if(contains) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不允许包含字符串 "+value);
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 不允许包含指定的字符串
         * */
        public  ValueWrapper<T> requireNotContains(T value) {
            return requireNotContains(null,value);
        }


        /**
         * 要求以指定的字符串开头
         * */
        public  ValueWrapper<T> requireStartsWith(String message,T value) {
            Result result=null;
            if(value==null || (value!=null && !(value instanceof String))) {
                throw new IllegalArgumentException("仅支持字符串类型的比较");
            }
            boolean startsWith=false;
            if(this.value==null || value==null) {
                startsWith = false;
            } else   {
                startsWith=((String)this.value).startsWith((String)value);
            }
            if(!startsWith) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("要求以 "+value+" 开头");
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求以指定的字符串开头
         * */
        public  ValueWrapper<T> requireStartsWith(T value) {
            return requireStartsWith(null,value);
        }

        /**
         * 不允许以指定的字符串开头
         * */
        public  ValueWrapper<T> requireNotStartsWith(String message,T value) {
            Result result=null;
            if(value==null || (value!=null && !(value instanceof String))) {
                throw new IllegalArgumentException("仅支持字符串类型的比较");
            }
            boolean startsWith=false;
            if(this.value==null || value==null) {
                startsWith = false;
            } else   {
                startsWith=((String)this.value).startsWith((String)value);
            }
            if(startsWith) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不允许以 "+value+" 开头");
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         *不允许以指定的字符串开头
         * */
        public  ValueWrapper<T> requireNotStartsWith(T value) {
            return requireNotStartsWith(null,value);
        }



        /**
         * 要求以指定的字符串结尾
         * */
        public  ValueWrapper<T> requireEndsWith(String message,T value) {
            Result result=null;
            if(value==null || (value!=null && !(value instanceof String))) {
                throw new IllegalArgumentException("仅支持字符串类型的比较");
            }
            boolean endsWith=false;
            if(this.value==null || value==null) {
                endsWith = false;
            } else   {
                endsWith=((String)this.value).endsWith((String)value);
            }
            if(!endsWith) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("要求以 "+value+" 结尾");
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求以指定的字符串结尾
         * */
        public  ValueWrapper<T> requireEndsWith(T value) {
            return requireEndsWith(null,value);
        }


        /**
         *不允许以指定的字符串结尾
         * */
        public  ValueWrapper<T> requireNotEndsWith(String message,T value) {
            Result result=null;
            if(value==null || (value!=null && !(value instanceof String))) {
                throw new IllegalArgumentException("仅支持字符串类型的比较");
            }
            boolean endsWith=false;
            if(this.value==null || value==null) {
                endsWith = false;
            } else   {
                endsWith=((String)this.value).endsWith((String)value);
            }
            if(endsWith) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不允许以 "+value+" 结尾");
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 不允许以指定的字符串结尾
         * */
        public  ValueWrapper<T> requireNotEndsWith(T value) {
            return requireNotEndsWith(null,value);
        }



        /**
         * 要求两个值相等
         * */
        public  ValueWrapper<T> requireNotEqual(String message,T value) {
            Result result=null;
            boolean equals=true;
            if(this.value==null || value == null)
            {
                equals = false;
            }
            else {
                equals=this.value.equals(value);
            }

            if(equals) {
                result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("值要求不等于 "+value);
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求两个值不等
         * */
        public  ValueWrapper<T> requireNotEqual(T value) {
            return requireNotEqual(null,value);
        }


        /**
         * 要求非 null 值
         * */
        public  ValueWrapper<T> requireNotNull() {
            return requireNotNull(null);
        }



        /**
         *  值必须在指定的列表范围内
         * */
        public ValueWrapper<T> requireNotInList(T... value) {
            return requireNotInList("",value);
        }

        /**
         * 值必须在指定的列表范围内
         */
        public ValueWrapper<T> requireNotInList(String message, T... value) {
            for (T o : value) {
                if (o == null) continue;
                if (o.equals(this.value)) {
                    Result result = ErrorDesc.failure(CommonError.VALUE_CAN_NOT_IN_LIST).subject(subject);
                    if (StringUtil.hasContent(message)) {
                        result.message(message);
                    }
                    this.validator.errors.add(result);
                    break;
                }
            }
            return this;
        }

        /**
         *  值必须在指定的列表范围内
         * */
        public ValueWrapper<T> requireInList(T... value) {
            return  requireInList("",value);
        }

        /**
         *  值必须在指定的列表范围内
         * */
        public ValueWrapper<T> requireInList(String message, T... value) {
            boolean isInList = false;
            for (T o : value) {
                if (o == null) continue;
                if (o.equals(this.value)) {
                    isInList = true;
                    break;
                }
            }

            if(!isInList) {
                Result result = ErrorDesc.failure(CommonError.VALUE_MUST_IN_LIST).subject(subject);
                if (StringUtil.hasContent(message)) {
                    result.message(message);
                }
                result.refer("allowed",StringUtil.join(value,","));
                this.validator.errors.add(result);
            }
            return this;
        }


        /**
         * 要求小于指定值
         * */
        public ValueWrapper<T> requireLessThan(T value) {
            return requireLessThan("",value);
        }

        /**
         * 要求小于指定值
         * */
        public ValueWrapper<T> requireLessThan(String message, T value) {
            boolean r=true;
            String msg=null;
            if(this.value==null || value==null) {
                r=false;
                msg = "比较值不允许为null";
            }
            else if(this.value instanceof Date) {
                Long thisValueT=((Date)this.value).getTime();
                Long targetValueT=((Date)value).getTime();
                if(thisValueT>=targetValueT) {
                    r=false;
                    msg = "必须小于 "+ DateUtil.getFormattedTime(false);
                }
            }
            else {
                try {
                    BigDecimal a = DataParser.parseBigDecimal(this.value);
                    BigDecimal b = DataParser.parseBigDecimal(value);
                    if (a.compareTo(b) != -1) {
                        r = false;
                        msg = "必须小于 " + value;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("不支持 "+this.value.getClass().getSimpleName()+" 类型的比较");
                }
            }
            if(!r) {
                Result result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message(msg);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }


        /**
         * 要求不小于指定值
         * */
        public ValueWrapper<T> requireNotLessThan(T value) {
            return requireNotLessThan("",value);
        }

        /**
         * 要求不小于指定值
         * */
        public ValueWrapper<T> requireNotLessThan(String message, T value) {
            boolean r=true;
            String msg=null;
            if(this.value==null || value==null) {
                r=false;
                msg = "比较值不允许为null";
            }
            else if(this.value instanceof Date) {
                Long thisValueT=((Date)this.value).getTime();
                Long targetValueT=((Date)value).getTime();
                if(thisValueT>=targetValueT) {
                    r=false;
                    msg = "必须不小于 "+ DateUtil.getFormattedTime(false);
                }
            }
            else {
                try {
                    BigDecimal a = DataParser.parseBigDecimal(this.value);
                    BigDecimal b = DataParser.parseBigDecimal(value);
                    if (a.compareTo(b) != -1) {
                        r = false;
                        msg = "必须不小于 " + value;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("不支持 "+this.value.getClass().getSimpleName()+" 类型的比较");
                }
            }
            if(r) {
                Result result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message(msg);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }

        /**
         * 要求大于指定值
         * */
        public ValueWrapper<T> requireGreaterThan(T value) {
            return requireGreaterThan("",value);
        }

        /**
         * 要求大于指定值
         * */
        public ValueWrapper<T> requireGreaterThan(String message, T value) {
            boolean r=true;
            String msg=null;
            if(this.value==null || value==null) {
                r=false;
                msg = "比较值不允许为null";
            }
            else if(this.value instanceof Date) {
                Long thisValueT=((Date)this.value).getTime();
                Long targetValueT=((Date)value).getTime();
                if(thisValueT<=targetValueT) {
                    r=false;
                    msg = "必须大于 "+ DateUtil.getFormattedTime(false);
                }
            }
            else {
                try {
                    BigDecimal a = DataParser.parseBigDecimal(this.value);
                    BigDecimal b = DataParser.parseBigDecimal(value);
                    if (a.compareTo(b) != 1) {
                        r = false;
                        msg = "必须大于 " + value;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("不支持 "+this.value.getClass().getSimpleName()+" 类型的比较");
                }
            }
            if(!r) {
                Result result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message(msg);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }


        /**
         * 要求不大于指定值
         * */
        public ValueWrapper<T> requireNotGreaterThan(T value) {
            return requireNotGreaterThan("",value);
        }

        /**
         * 要求不大于指定值
         * */
        public ValueWrapper<T> requireNotGreaterThan(String message, T value) {
            boolean r=true;
            String msg=null;
            if(this.value==null || value==null) {
                r=false;
                msg = "比较值不允许为null";
            }
            else if(this.value instanceof Date) {
                Long thisValueT=((Date)this.value).getTime();
                Long targetValueT=((Date)value).getTime();
                if(thisValueT<=targetValueT) {
                    r=false;
                    msg = "必须不大于 "+ DateUtil.getFormattedTime(false);
                }
            }
            else {
                try {
                    BigDecimal a = DataParser.parseBigDecimal(this.value);
                    BigDecimal b = DataParser.parseBigDecimal(value);
                    if (a.compareTo(b) != 1) {
                        r = false;
                        msg = "必须不大于 " + value;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("不支持 "+this.value.getClass().getSimpleName()+" 类型的比较");
                }
            }
            if(r) {
                Result result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message(msg);
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }







        private static  String EMAIL_PATTERN="[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+";


        /**
         * 要求邮箱地址
         * */
        public ValueWrapper<T> requireEmail() {
            return requireEmail("");
        }
        /**
         * 要求邮箱地址
         * */
        public ValueWrapper<T> requireEmail(String message) {
            boolean r=true;
            String msg=null;
            if(this.value==null || value==null) {
                r=false;
                msg = "比较值不允许为null";
            }

            if(!(this.value instanceof String)) {
                throw new IllegalArgumentException("不支持的类型");
            }



            boolean isEmail = ((String)this.value).matches(EMAIL_PATTERN);
            if(!isEmail) {
                Result result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不是一个有效的邮箱地址");
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }


        private static Pattern URL_PATTERN = Pattern.compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");

        /**
         * 要求邮箱地址
         * */
        public ValueWrapper<T> requireHttpUrl() {
            return requireHttpUrl("");
        }
        /**
         * 要求邮箱地址
         * */
        public ValueWrapper<T> requireHttpUrl(String message) {
            boolean r=true;
            String msg=null;
            if(this.value==null || value==null) {
                r=false;
                msg = "比较值不允许为null";
            }

            if(!(this.value instanceof String)) {
                throw new IllegalArgumentException("不支持的类型");
            }

            boolean isURL = URL_PATTERN.matcher((String)this.value).matches();
            if(!isURL) {
                Result result = ErrorDesc.failure(CommonError.PARAM_VALUE_INVALID).subject(subject).message("不是一个有效的URL地址");
                if(StringUtil.hasContent(message)) {
                    result.message(message);
                }
                this.validator.errors.add(result);
            }
            return this;
        }



        /**
         * 校验值，如 this.validator().forValue(instance.getTitle(),"标题(title)").require();
         * */
        public <T> ValueWrapper asserts(T value,String subject) {
            return this.validator().asserts(value,subject);
        }

        /**
         * 校验值，如 this.validator().forValue(instance.getTitle()).require();
         * */
        public <T> ValueWrapper asserts(T value) {
            return this.validator().asserts(value);
        }

        public boolean failure() {
            return this.validator().failure();
        }

        public ValueWrapper<T> validate(SingleValueValidatorFunction function) {
            Result result=function.validate(this.value);
            if(result.success()) return this;
            else {
                this.validator.errors.add(result);
            }
            return this;
        }

    }



    private List<Result> errors = new ArrayList<>();

    /**
     * 校验值，如 this.validator().forValue(instance.getTitle(),"标题(title)").require();
     * */
    public <T> ValueWrapper<T> asserts(T value,String subject) {
        return new ValueWrapper<T>(this,subject,value);
    }

    /**
     * 校验值，如 this.validator().forValue(instance.getTitle()).require();
     * */
    public <T> ValueWrapper<T> asserts(T value) {
        return new ValueWrapper<T>(this,null,value);
    }

    /**
     * 是否校验成功
     * */
    public boolean success() {
        return  errors.isEmpty();
    }

    /**
     * 是否校验失败
     * */
    public boolean failure() {
        return !success();
    }

    /**
     * 返回合并的校验结果
     * */
    public Result getMergedResult() {
        return getMergedResult(CommonError.PARAM_VALUE_INVALID,null);
    }

    /**
     * 返回合并的校验结果
     * */
    public Result getMergedResult(String code) {
        return getMergedResult(code,null);
    }

    /**
     * 返回合并的校验结果
     * */
    public Result getMergedResult(String code,String message) {
        Result result=new Result();
        result.success(this.success());
        result.code(code);
        if(StringUtil.hasContent(message)) {
            result.message(message);
        }
        for (Result error : errors) {
            result.addError(error);
        }
        return result;
    }

    /**
     * 获得所有的校验失败的结果
     * */
    public List<Result> getErrors() {
        return errors;
    }

    /**
     * 返回第一个校验错误的结果
     * */
    public Result getFirstResult() {
        if(this.errors.isEmpty()) return null;
        return errors.get(0);
    }

    public void clearErrors() {
        this.errors.clear();
    }

}
