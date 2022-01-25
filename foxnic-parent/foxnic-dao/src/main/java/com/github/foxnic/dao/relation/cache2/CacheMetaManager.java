package com.github.foxnic.dao.relation.cache2;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.dao.cache.DataCacheManager;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.relation.RelationSolver;
import com.github.foxnic.dao.spec.DAO;

import java.util.*;

public class CacheMetaManager {

    private static final CacheMetaManager instance =new CacheMetaManager();

    public static CacheMetaManager instance() {
        return instance;
    }

    private CacheMetaManager() {}

    private static Map<Class, Map<String, CacheMeta>> TYPED_META_MAP = new HashMap<>();
    private Map<String, Set<CacheMeta>> TABLED_META_MAP= null;


    private Map<String, CacheMeta> getTypeMeta(PropertyRoute route) {
        Map<String, CacheMeta> typeMeta= TYPED_META_MAP.get(route.getMasterPoType());
        if(typeMeta==null) {
            typeMeta=new HashMap<>();
            TYPED_META_MAP.put(route.getMasterPoType(),typeMeta);
        }
        return typeMeta;
    }

    private DoubleCache<String, Object> metaCache;

    public DoubleCache<String, Object> getMetaCache() {
        return metaCache;
    }

    private static  LocalCache<String,Map<String,Map<String,String>>> joinedTableFieldsCache = new LocalCache();
    private static  LocalCache<String,Map<String,Map<String,String>>> joinedTablePksCache = new LocalCache();

    /**
     *  保存缓存 Meta 以及数据
     * */
    public void save(DAO dao, Entity owner, PropertyRoute route, List value, List<Rcd> rcds) {

        String jkey=route.getSlavePoType().getName()+"."+route.getProperty();
        // 表 -> 字段 ->  列别名
        Map<String,Map<String,String>> joinedTableFields=joinedTableFieldsCache.get(jkey);
        // 表 -> 主键
        Map<String,Map<String,String>> joinedTablePks=joinedTablePksCache.get(jkey);

        if(joinedTableFields==null && joinedTablePks==null) {
            //
            joinedTableFields=new HashMap<>();
            joinedTablePks=new HashMap<>();
            // 形成 参与 表连接的字段的结构
            if (rcds != null && !rcds.isEmpty()) {
                List<String> fields = rcds.get(0).getOwnerSet().getFields();
                String[] tmp = null;
                for (String field : fields) {
                    String oField = field;
                    if (field.startsWith(RelationSolver.JOIN_FS) && field.contains(RelationSolver.JOIN_FS_RV)) {
                        field = field.substring(RelationSolver.JOIN_FS.length()).toLowerCase();
                        tmp = field.split(RelationSolver.JOIN_FS_RV);
                        Map<String, String> relationFields = joinedTableFields.get(tmp[0]);
                        if (relationFields == null) {
                            relationFields = new HashMap<>();
                            joinedTableFields.put(tmp[0], relationFields);
                        }
                        relationFields.put(tmp[1], oField);
                    }
                    //
                    if (field.startsWith(RelationSolver.PK_JOIN_FS) && field.contains(RelationSolver.JOIN_FS_RV)) {
                        field = field.substring((RelationSolver.PK_JOIN_FS).length()).toLowerCase();
                        tmp = field.split(RelationSolver.JOIN_FS_RV);
                        Map<String,String> pkFields = joinedTablePks.get(tmp[0]);
                        if (pkFields == null) {
                            pkFields = new HashMap<>();
                            joinedTablePks.put(tmp[0], pkFields);
                        }
                        pkFields.put(tmp[1],oField);
                    }
                }
            }
            joinedTableFieldsCache.put(jkey,joinedTableFields);
            joinedTablePksCache.put(jkey,joinedTablePks);
        }


        Map<String,Map<String,Set>> joinedTablePkValues=new HashMap<>();
        Map<String,Map<String,Set>> joinedTableFieldValues=new HashMap<>();

        // 采集中间值(关联字段)
        for (Map.Entry<String,Map<String,String>> table : joinedTableFields.entrySet()) {
            Map<String,Set> tableData=joinedTableFieldValues.get(table.getKey());
            if(tableData==null) {
                tableData=new HashMap<>();
                joinedTableFieldValues.put(table.getKey(),tableData);
            }
            for (Map.Entry<String,String> field : table.getValue().entrySet()) {
                Set set=tableData.get(field.getKey());
                if(set==null) {
                    set=new HashSet();
                    tableData.put(field.getKey(),set);
                }
                if(rcds!=null) {
                    for (Rcd rcd : rcds) {
                        Object val = rcd.getValue(field.getValue());
                        set.add(val);
                    }
                }
            }
        }

        // 采集中间值(主键)
        for (Map.Entry<String,Map<String,String>> table : joinedTablePks.entrySet()) {
            Map<String,Set> tableData=joinedTablePkValues.get(table.getKey());
            if(tableData==null) {
                tableData=new HashMap<>();
                joinedTablePkValues.put(table.getKey(),tableData);
            }
            for (Map.Entry<String,String> field : table.getValue().entrySet()) {
                Set set=tableData.get(field.getKey());
                if(set==null) {
                    set=new HashSet();
                    tableData.put(field.getKey(),set);
                }
                if(rcds!=null) {
                    for (Rcd rcd : rcds) {
                        Object val = rcd.getValue(field.getValue());
                        set.add(val);
                    }
                }
            }
        }


        // 生成 CacheMeta
        Map<String, CacheMeta> typeMeta = getTypeMeta(route);

        // 设置主键
        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey= buildMetaKey(route.getProperty(),tm,(Entity) owner);

        // 构建按数据表索引
        CacheMeta cacheMeta=typeMeta.get(metaKey);
        if(cacheMeta==null) {
            cacheMeta = new CacheMeta(route.getMasterPoType(), route.getProperty(), joinedTablePks, joinedTableFields);
            for (String table : joinedTableFields.keySet()) {
                Set metas=TABLED_META_MAP.get(table);
                if(metas==null) {
                    metas=new HashSet();
                    TABLED_META_MAP.put(table,metas);
                }
                metas.add(cacheMeta);
            }

            // 主表
            String table=route.getMasterTable().name().toLowerCase();
            Set metas=TABLED_META_MAP.get(table);
            if(metas==null) {
                metas=new HashSet();
                TABLED_META_MAP.put(table,metas);
            }
            metas.add(cacheMeta);

        }

        // 填充数据
        cacheMeta.setValues(joinedTablePkValues,joinedTableFieldValues);

        List<DBColumnMeta> pks=tm.getPKColumns();
        for (DBColumnMeta pk : pks) {
            cacheMeta.setOwnerId(pk.getColumn(),BeanUtil.getFieldValue(owner,pk.getColumn()));
        }

        // 缓存 CacheUnit
        metaKey=cacheMeta.getMetaKey();
        typeMeta.put(metaKey,cacheMeta);
        // 缓存属性数据
        String dataKey=route.getMasterPoType().getName()+":"+metaKey;
        DoubleCache<String,Object> cache=dao.getDataCacheManager().getEntityCache(route.getMasterPoType());
        cache.put(dataKey,value);

        cacheMeta.setValueCacheKey(dataKey);

        IS_META_READY.put(route.getKey(),true);

        saveMetas(dao.getDataCacheManager());

    }

    private  static Map<String,Boolean> IS_META_READY = null;

    private void initMetas(DataCacheManager dataCacheManager) {
        DoubleCache metaCache=dataCacheManager.getMetaCache();
        if(IS_META_READY ==null) IS_META_READY =(Map<String,Boolean>)metaCache.get("meta_ready_flag");
        if(IS_META_READY ==null) IS_META_READY=new HashMap<>();
        //
        if(TABLED_META_MAP==null) TABLED_META_MAP= (Map<String, Set<CacheMeta>> )metaCache.get("tabled_meta");
        if(TABLED_META_MAP==null) TABLED_META_MAP= new HashMap<>();
    }

    private void saveMetas(DataCacheManager dataCacheManager) {
        SimpleTaskManager.doParallelTask(new Runnable() {
            @Override
            public void run() {
                DoubleCache metaCache=dataCacheManager.getMetaCache();
                if(IS_META_READY !=null) metaCache.put("meta_ready_flag", IS_META_READY);
                if(TABLED_META_MAP!=null) metaCache.put("tabled_meta",TABLED_META_MAP);
            }
        });
    }

    /**
     * 实体关系预构建
     * */
    public Collection<? extends Entity> preBuild(DAO dao,Collection pos, PropertyRoute route) {


        initMetas(dao.getDataCacheManager());

        Boolean isMetaReady= IS_META_READY.get(route.getKey());
        if(isMetaReady==null || isMetaReady ==false) {
            return new ArrayList<>();
        }

        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey = null;
        String dataKey = null;
        List cachedValue;
        Collection built=new ArrayList<>();
        for (Object po : pos) {
            metaKey= buildMetaKey(route.getProperty(),tm,(Entity) po);
            dataKey=route.getMasterPoType().getName()+":"+metaKey;
            DoubleCache<String,Object> cache=dao.getDataCacheManager().getEntityCache(route.getMasterPoType());
            cachedValue=(List) cache.get(dataKey);
            if(cachedValue!=null) {
                if(route.isList()) {
                    BeanUtil.setFieldValue(po,route.getProperty(),cachedValue);
                } else {
                    if(!cachedValue.isEmpty()) {
                        BeanUtil.setFieldValue(po, route.getProperty(), cachedValue.get(0));
                    }
                }
                built.add(po);
            }
        }
        return built;
    }

    public void invalidJoinCache(CacheInvalidEventType eventType,DataCacheManager dcm, String table, Entity valueBefore, Entity valueAfter) {
        if(dcm==null) return;
        Class clz=null;
        if(valueBefore!=null) {
            clz=valueBefore.getClass();
        }
        if(clz==null && valueAfter!=null) {
            clz=valueAfter.getClass();
        }
        if(clz==null) return;
        while (EntityContext.isProxyType(clz)) {
            clz=clz.getSuperclass();
        }

        Set<CacheMeta> metas=this.TABLED_META_MAP.get(table);
        if(metas==null) {
            this.TABLED_META_MAP=null;
            initMetas(dcm);
        }
        metas=this.TABLED_META_MAP.get(table);

        if(metas==null || metas.isEmpty()) return;
        for (CacheMeta meta : metas) {
            DoubleCache ch=dcm.getEntityCache(clz);
            if(ch!=null && willInvalid(eventType,meta,table,clz,valueBefore,valueAfter)) {
                ch.remove(meta.getValueCacheKey(),false);
            }
        }
    }

    /**
     * 判断是否需要使缓存失效失效
     * */
    private boolean willInvalid(CacheInvalidEventType eventType,CacheMeta meta,String table,Class poType ,Entity valueBefore, Entity valueAfter) {
        return new CacheInvalidDecider(eventType,meta,table,poType,valueBefore,valueAfter).decide();
    }

    public static String buildMetaKey(String property,DBTableMeta tm, Entity entity) {
        String key=property+":";
        List<DBColumnMeta> pks=tm.getPKColumns();
        int i=0;
        for (DBColumnMeta pk : pks) {
            key+=pk.getColumn().toLowerCase()+"="+BeanUtil.getFieldValue(entity,pk.getColumn());
            if(i<pks.size()-1) {
                key+=",";
            }
            i++;
        }
        return key;
    }
}
