package com.github.foxnic.commons.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JSONWrapper {

    private JSON value;
    private JSONWrapper parent;
    private JSONWrapper top;


    private JSONWrapper property(Object key,int type) {

        if(value==null) {
            initValueByKey(key);
            this.top=this;
        }

        JSON childValue=null;
        if(type==0) {
            if(key instanceof String) {
                childValue=((JSONObject)value).getJSONObject((String) key);
            } else if(key instanceof Integer) {
                try {
                    childValue = ((JSONArray) value).getJSONObject((Integer) key);
                } catch (Exception e){}
            }
            if(childValue==null) {
                childValue=new JSONObject();
            }
        } else {
            if(key instanceof String) {
                childValue=((JSONObject)value).getJSONArray((String) key);
            } else if(key instanceof Integer) {
                try {
                    childValue = ((JSONArray) value).getJSONArray((Integer) key);
                } catch (Exception e) {}
            }

            if(childValue==null) {
                childValue=new JSONArray();
            }
        }
        this.setPropertyValue(key,childValue);
        return makePropertyWrapper(childValue);
    }


    public JSONWrapper object(Integer index) {
         return property(index,0);
    }

    public JSONWrapper object(String key) {
        return property(key,0);
    }

    public JSONWrapper array(Integer index) {
        return property(index,1);
    }

    public JSONWrapper array(String key) {
        return property(key,1);
    }

    private void initValueByKey(Object key) {
        if(value==null) {
            if (key instanceof String) {
                value = new JSONObject();
            } else if (key instanceof Integer) {
                value = new JSONArray();
            }
        }
    }

    private JSONWrapper makePropertyWrapper(JSON value) {
        JSONWrapper childWrapper=new JSONWrapper();
        childWrapper.value=value;
        childWrapper.parent=this;
        childWrapper.top=this.top;
        return childWrapper;
    }

    private void setPropertyValue(Object key,Object value) {
        initValueByKey(key);
        if(key instanceof Integer) {
            ((JSONArray)this.value).set((Integer)key,value);
        } else if(key instanceof String) {
            ((JSONObject)this.value).put((String) key,value);
        }
    }


    public JSONWrapper put(String key, Object value) {
        setPropertyValue(key,value);
        return this;
    }

    public JSONWrapper set(Integer key, Object value) {
        setPropertyValue(key,value);
        return this;
    }

    public JSON value() {
        return this.value;
    }

}
