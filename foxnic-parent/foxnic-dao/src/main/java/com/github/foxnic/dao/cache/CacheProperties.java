package com.github.foxnic.dao.cache;


import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

import java.util.*;


public class CacheProperties {

    private static final  String DETAIL_PREFIX="foxnic.cache";
    private String modeName;

    private DataCacheManager.JoinCacheMode mode;

    /**
     * 是否为 join 使用缓存
     * */
    private Integer localLimit =512;
    private Integer localExpire=1000 * 60 *20;
    private Integer remoteExpire=1000 * 60 *20;
    private PoCacheProperty defaultPoCacheProperty;
    private Integer detection =512;

    private <T> T getConfigValue(Map<String,Object> configs,String subKey,Class<T> type,T def) {
        T value=BeanUtil.getFieldValue(configs.get(DETAIL_PREFIX+"."+subKey),"value",type);
        if(value!=null) return value;
        return def;
    }

    private Set<String> getPoNames(Map<String,Object> configs) {
        Set<String> poNames=new HashSet<>();
        for (String key : configs.keySet()) {
            if(!key.startsWith(DETAIL_PREFIX+".details.")) continue;
            String p=null;
            if(key.contains(".mode")) p=".mode";
            if(key.contains(".local-limit")) p=".local-limit";
            if(key.contains(".local-expire")) p=".local-expire";
            if(key.contains(".remote-expire")) p=".remote-expire";
            if(key.contains(".strategy")) p=".strategy";
            if(p==null) continue;
            key=key.substring((DETAIL_PREFIX+".details.").length(),key.indexOf(p));
            poNames.add(key);
        }
        return  poNames;
    }

    public CacheProperties(Map<String,Object> configs) {

        modeName = getConfigValue(configs,"default.mode",String.class,"none");
        mode = DataCacheManager.JoinCacheMode.valueOf(modeName);
        localLimit=getConfigValue(configs,"default.local-limit",Integer.class,512);
        localExpire=getConfigValue(configs,"default.local-expire",Integer.class,1000 * 60 *20);
        remoteExpire=getConfigValue(configs,"default.remote-expire",Integer.class,1000 * 60 *20);

        Set<String> poNames = this.getPoNames(configs);

        for (String poName : poNames) {
            PoCacheProperty property=new PoCacheProperty(this,configs,poName);
            poCachePropertyMap.put(property.getPoType(),property);
        }

        defaultPoCacheProperty=new PoCacheProperty(this);

    }

    public DataCacheManager.JoinCacheMode getMode() {
        if(mode==null) {
            mode=DataCacheManager.JoinCacheMode.valueOf(modeName);
        }
        return mode;
    }

    public Integer getLocalLimit() {
        return localLimit;
    }

    public Integer getLocalExpire() {
        return localExpire;
    }

    public Integer getRemoteExpire() {
        return remoteExpire;
    }

    private Map<Class,PoCacheProperty> poCachePropertyMap=new HashMap<>();


    public PoCacheProperty getPoCacheProperty(Class poType) {
        PoCacheProperty property=poCachePropertyMap.get(poType);
        if(property==null) {
            return defaultPoCacheProperty;
        }
        return property;
    }

    public Map<Class, PoCacheProperty> getPoCachePropertyMap() {
        return poCachePropertyMap;
    }

    /**
     * detail 的 Po 类配置
     * */
    public static class PoCacheProperty {

        private Map<String,Object> configs;
        private CacheProperties cacheProperties;
        private String prefix=null;
        private Class poType=null;
        private DataCacheManager.JoinCacheMode mode;

        private  Integer localLimit =512;
        private  Integer localExpire=1000 * 60 *20;
        private  Integer remoteExpire=1000 * 60 *20;

        private Map<String,CacheStrategy> cacheStrategyMap=new HashMap<>();

        public PoCacheProperty(CacheProperties cacheProperties){
            this.mode=cacheProperties.getMode();
            this.localLimit=cacheProperties.getLocalLimit();
            this.localExpire=cacheProperties.getLocalExpire();
            this.remoteExpire=cacheProperties.getRemoteExpire();
        };

        public PoCacheProperty(CacheProperties cacheProperties,Map<String,Object> configs,String poName) {
            this.prefix=DETAIL_PREFIX+".details."+poName;
            this.poType= ReflectUtil.forName(poName);
            this.cacheProperties=cacheProperties;
            String modeName = getConfigValue(configs,"mode",String.class,null);
            if(!StringUtil.isBlank(modeName)) {
                this.mode=DataCacheManager.JoinCacheMode.valueOf(modeName);
            } else {
                this.mode=cacheProperties.getMode();
            }

            localLimit=getConfigValue(configs,"local-limit",Integer.class,cacheProperties.getLocalLimit());
            localExpire=getConfigValue(configs,"local-expire",Integer.class,cacheProperties.getLocalExpire());
            remoteExpire=getConfigValue(configs,"remote-expire",Integer.class,cacheProperties.getRemoteExpire());

            //搜集自定义策略
            Set<String> strategyNames=getStrategyNames(configs);
            for (String strategyName : strategyNames) {
                Boolean isAccurate=getConfigValue(configs,"strategy."+strategyName+".is-accurate",Boolean.class,true);
                Boolean cacheEmptyResult=getConfigValue(configs,"strategy."+strategyName+".local-expire",Boolean.class,false);
                String properties = getConfigValue(configs,"strategy."+strategyName+".properties",String.class,null);
                if(StringUtil.isBlank(properties)) {
                    throw new IllegalArgumentException("自定义缓存配置，至少包含一个属性 : "+this.prefix+"."+strategyName);
                }
                String[] props=properties.split(",");
                List<String> pps = BeanUtil.getAllFieldNames(this.poType);
                for (String prop : props) {
                    if(!pps.contains(prop)) {
                        throw new IllegalArgumentException("缓存策略属性 "+prop+" 不是 "+poName+" 的属性 : "+this.prefix+"."+strategyName+".properties = "+properties);
                    }
                }
                CacheStrategy strategy=new CacheStrategy(strategyName,isAccurate,cacheEmptyResult,props);
                cacheStrategyMap.put(strategyName,strategy);
            }

        }

        public Map<String, CacheStrategy> getCacheStrategyMap() {
            return cacheStrategyMap;
        }

        private Set<String> getStrategyNames(Map<String,Object> configs) {
            Set<String> poNames=new HashSet<>();
            for (String key : configs.keySet()) {
                if(!key.startsWith(prefix+".strategy.")) continue;
                String p=null;
                if(key.contains(".is-accurate")) p=".is-accurate";
                if(key.contains(".cache-empty-result")) p=".cache-empty-result";
                if(key.contains(".properties")) p=".properties";
                if(p==null) continue;
                key=key.substring((prefix+".strategy.").length(),key.indexOf(p));
                poNames.add(key);
            }
            return  poNames;
        }

        private  <T> T getConfigValue(Map<String,Object> configs,String subKey,Class<T> type,T def) {
            T value=BeanUtil.getFieldValue(configs.get(prefix+"."+subKey),"value",type);
            if(value!=null) return value;
            return def;
        }

        public Class getPoType() {
            return poType;
        }

        public DataCacheManager.JoinCacheMode getMode() {
            return mode;
        }

        public Integer getLocalLimit() {
            return localLimit;
        }

        public Integer getLocalExpire() {
            return localExpire;
        }

        public Integer getRemoteExpire() {
            return remoteExpire;
        }

    }




}
