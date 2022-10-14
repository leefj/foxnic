package com.github.foxnic.springboot.api.swagger.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.lang.DataParser;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ApiDocket {

    private JSONObject data=null;

    public ApiDocket(JSONObject data) {
        this.data=data;
    }



    public JSONArray getApiTags() {
        JSONArray tags = data.getJSONArray("tags");
        if(tags==null) tags=new JSONArray();
        return tags;
    }

    public JSONObject getApiPaths() {
        JSONObject paths = data.getJSONObject("paths");
        return paths;
    }

    public JSONObject getApiDefinitions() {
        JSONObject definitions = data.getJSONObject("definitions");
        if(definitions==null){
            definitions=new JSONObject();
            data.put("definitions",definitions);
        }
        return definitions;
    }

    public JSONObject getApiResponses(JSONObject httpMethodEl) {
        // 处理响应码
        return httpMethodEl.getJSONObject("responses");
    }

    public JSONObject getApiResponse200(JSONObject httpMethodEl) {
        JSONObject responses=this.getApiResponses(httpMethodEl);
        if(responses==null) return null;
        return responses.getJSONObject("200");
    }

    public void setApiElement(String key, Object element) {
        data.put(key,element);
    }

    public String getApiDoc() {
        return data.toJSONString();
    }

    public static String getTypeName(Class type) {
        if (String.class.equals(type) || DataParser.isDateTimeType(type)) {
            return "string";
        } else if (Integer.class.equals(type) || Long.class.equals(type) || Short.class.equals(type) || BigInteger.class.equals(type)) {
            return "integer";
        } else if (Float.class.equals(type) || Double.class.equals(type) || BigDecimal.class.equals(type)) {
            return "number";
        } else if (Boolean.class.equals(type)) {
            return "boolean";
        } else if (DataParser.isArray(type)) {
            return "array";
        } else if (DataParser.isList(type) || DataParser.isSet(type)) {
            return "array";
        } else if (DataParser.isMap(type)) {
            return "object";
        } else {
            return "object";
        }
    }


}
