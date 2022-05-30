package com.github.foxnic.commons.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.JSON;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author fangjieli
 */
public class JSONUtil {

    /**
     * 格式化 json
     * @param jsonstr 输入的JSON字符串
     * @return 格式化后的JSON字符串
     * */
    public static String format(String jsonstr)
    {
        return JSONFormater.format(jsonstr);
    }

    /**
     * 格式化 json
     * @param json 输入的JSON对象
     * @return 格式化后的JSON字符串
     * */
    public static String format(JSONObject json)
    {
        return JSONFormater.format(json.toJSONString());
    }

    /**
     * 格式化 json
     * @param json 输入的JSON数组
     * @return 格式化后的JSON字符串
     * */
    public static String format(JSONArray json)
    {
        return JSONFormater.format(json.toJSONString());
    }


    /**
     * xml转json
     * @param xml xml字符串
     * @return Json对象
     * @throws DocumentException 异常
     */
    public static JSONObject parseFromXML(String xml) throws DocumentException{
        Document doc= DocumentHelper.parseText(xml);
        return parseFromXML(doc);
    }


    /**
     * xml转json
     * @param doc  xml文档
     * @return Json对象
     */
    public static JSONObject parseFromXML(Document doc) {
        JSONObject json=new JSONObject(true);
        dom2JSON(doc.getRootElement(), json);
        return json;
    }



    /**
     * xml转json
     * @param element
     * @param json
     */
    private static void dom2JSON(Element element,JSONObject json){
        //如果是属性
        for(Object o:element.attributes()){
            Attribute attr=(Attribute)o;
            if(!isEmpty(attr.getValue())){
                json.put("@"+attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl=element.elements();
        if(chdEl.isEmpty()&&!isEmpty(element.getText())){//如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for(Element e:chdEl){//有子元素
            if(!e.elements().isEmpty()){//子元素也有子元素
                JSONObject chdjson=new JSONObject();
                dom2JSON(e,chdjson);
                Object o=json.get(e.getName());
                if(o!=null){
                    JSONArray jsona=null;
                    if(o instanceof JSONObject){//如果此元素已存在,则转为jsonArray
                        JSONObject jsono=(JSONObject)o;
                        json.remove(e.getName());
                        jsona=new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if(o instanceof JSONArray){
                        jsona=(JSONArray)o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                }else{
                    if(!chdjson.isEmpty()){
                        json.put(e.getName(), chdjson);
                    }
                }


            }else{//子元素没有子元素
                for(Object o:element.attributes()){
                    Attribute attr=(Attribute)o;
                    if(!isEmpty(attr.getValue())){
                        json.put("@"+attr.getName(), attr.getValue());
                    }
                }
                if(!e.getText().isEmpty()){
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.trim().isEmpty() || "null".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * 将 Java 对象转换成 json string
     * */
    public static String toJSONString(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * 将  json string 对象转换成 JSONObject
     * */
    public static JSONObject parseJSONObject(String json) {
        return JSON.parseObject(json);
    }

    /**
     * 将  json array string 对象转换成 JSONArray
     * */
    public static JSONArray parseJSONArray(String array) {
        return JSON.parseArray(array);
    }

    /**
     * 将  json array string 对象转换成 JSONArray
     * */
    public static <T> T toJavaBean(String json,Class<T> type) {
        return toJavaBean(parseJSONObject(json),type);
    }

    /**
     * 将  json array string 对象转换成 JSONArray
     * */
    public static <T> T toJavaBean(JSONObject json,Class<T> type) {
        return json.toJavaObject(type);
    }

    /**
     * 将  json array string 对象转换成 JSONArray
     * */
    public static <T> List<T> toList(String array,Class<T> componentType) {
        return JSONObject.parseArray(array,componentType);
    }

    /**
     * 将  json array string 对象转换成 JSONArray
     * */
    public static <T> List<T> toList(JSONArray array,Class<T> componentType) {
        return array.toJavaList(componentType);
    }

    /**
     * 将 java bean 对象转换成 JSONArray
     * */
    public static JSONObject toJSONObject(Object bean) {
        return (JSONObject)JSON.toJSON(bean);
    }

    /**
     * 将  list 对象转换成 JSONArray
     * */
    public static JSONArray toJSONArray(List list) {
        return (JSONArray)JSON.toJSON(list);
    }










}
