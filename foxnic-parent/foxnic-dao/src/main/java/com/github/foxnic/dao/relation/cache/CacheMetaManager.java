package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.lang.StringUtil;
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
import java.util.concurrent.ConcurrentHashMap;

public class CacheMetaManager {

    public static final ThreadLocal<Set<String>> IDS_FROM_CACHE=new ThreadLocal();

    private static final CacheMetaManager instance =new CacheMetaManager();

    public static CacheMetaManager instance() {
        return instance;
    }

    private DataCacheManager dataCacheManager=null;


    private CacheMetaManager() {
        (new SimpleTaskManager()).doIntervalTask(new Runnable() {
            @Override
            public void run() {
                    saveMetas();
            }
        },1000);
    }

//    private Map<Class, Map<String, CacheMeta>> typedMetaMap = new HashMap<>();
    private Map<String, Set<CacheMeta>> tabledMetaMap = null;


//    private Map<String, CacheMeta> getTypeMeta(PropertyRoute route) {
//        Map<String, CacheMeta> typeMeta= typedMetaMap.get(route.getMasterPoType());
//        if(typeMeta==null) {
//            typeMeta=new HashMap<>();
//            typedMetaMap.put(route.getMasterPoType(),typeMeta);
//        }
//        return typeMeta;
//    }

    private DoubleCache<String, Object> metaCache;

    public DoubleCache<String, Object> getMetaCache() {
        return metaCache;
    }

    private  Map<String,Map<String,Map<String,String>>> joinedTableFieldsCache = null;
    private  Map<String,Map<String,Map<String,String>>> joinedTablePksCache = null;


    /**
     *  保存缓存 Meta 以及数据
     * */
    public void save(DAO dao, Entity owner, PropertyRoute route, List value, List<Rcd> rcds) {

        this.dataCacheManager=dao.getDataCacheManager();
        //缓存关闭时，无法拿到 cache 对象
        DoubleCache<String,Object> cache=dao.getDataCacheManager().getEntityCache(route.getMasterPoType());
        if(cache==null) return;

        if(!route.isCachePropertyData()) {
            return;
        }

        initMetas(dao.getDataCacheManager());

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
                            relationFields = new LinkedHashMap<>();
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
                            pkFields = new TreeMap<>();
                            joinedTablePks.put(tmp[0], pkFields);
                        }
                        pkFields.put(tmp[1],oField);
                    }
                }


            }
            joinedTableFieldsCache.put(jkey,joinedTableFields);
            joinedTablePksCache.put(jkey,joinedTablePks);
        }


        Map<String,Set> joinedTablePkValues=new HashMap<>();
        Map<String,Map<String,Set>> joinedTableFieldValues=new HashMap<>();

        // 采集中间值(关联字段)
        for (Map.Entry<String,Map<String,String>> table : joinedTableFields.entrySet()) {
            //
            Map<String,Set> tableData=joinedTableFieldValues.get(table.getKey());
            if(tableData==null) {
                tableData=new HashMap<>();
                joinedTableFieldValues.put(table.getKey(),tableData);
            }
            // 列顺序由 TreeMap 觉得
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
        for (Map.Entry<String, Map<String, String>> table : joinedTablePks.entrySet()) {
            Set tableData = joinedTablePkValues.get(table.getKey());
            if (tableData == null) {
                tableData = new HashSet();
                joinedTablePkValues.put(table.getKey(), tableData);
            }
            // 循环主键字段
            if (rcds != null) {
                for (Rcd rcd : rcds) {
                    int i=0;
                    Object[] vals=new Object[table.getValue().size()];
                    for (Map.Entry<String, String> field : table.getValue().entrySet()) {
                        Object val = rcd.getValue(field.getValue());
                        vals[i]=val;
                        i++;
                    }
                    tableData.add(StringUtil.join(vals,"-"));
                }
            }
        }


        // 生成 CacheMeta
//        Map<String, CacheMeta> typeMeta = getTypeMeta(route);

        // 设置主键
        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey= buildMetaKey(route.getProperty(),tm,(Entity) owner);

        // 构建按数据表索引
//        CacheMeta cacheMeta=typeMeta.get(metaKey);
//        if(cacheMeta==null) {
        CacheMeta cacheMeta = new CacheMeta(route.getMasterPoType(), route.getMasterTable().name(),route.getProperty(), joinedTablePks, joinedTableFields);
            //
            for (String table : joinedTableFields.keySet()) {
                Set metas= tabledMetaMap.get(table);
                if(metas==null) {
                    metas=new HashSet();
                    tabledMetaMap.put(table,metas);
                }
                metas.add(cacheMeta);
            }

            // 主表
            String table=route.getMasterTable().name().toLowerCase();
            Set metas= tabledMetaMap.get(table);
            if(metas==null) {
                metas=new HashSet();
                tabledMetaMap.put(table,metas);
            }
            metas.add(cacheMeta);

//        }

        // 填充数据
        cacheMeta.setValues(joinedTablePkValues,joinedTableFieldValues);

        List<DBColumnMeta> pks=tm.getPKColumns();
        for (DBColumnMeta pk : pks) {
            cacheMeta.setOwnerId(pk.getColumn(),BeanUtil.getFieldValue(owner,pk.getColumn()));
        }

        // 缓存 CacheUnit
        metaKey=cacheMeta.getMetaKey();
//        typeMeta.put(metaKey,cacheMeta);
        // 缓存属性数据
        String dataKey=route.getMasterPoType().getName()+":"+metaKey;

        cache.put(dataKey,value);

        cacheMeta.setValueCacheKey(dataKey);

        isMetaReadyFlags.put(route.getKey(),true);



    }

    private Map<String,Boolean> isMetaReadyFlags = null;

    private void initMetas(DataCacheManager dataCacheManager) {

//        synchronized (instance) {
            DoubleCache metaCache = dataCacheManager.getMetaCache();
            //
            if (isMetaReadyFlags == null) isMetaReadyFlags = (Map<String, Boolean>) metaCache.get("meta_ready_flag");
            if (isMetaReadyFlags == null) isMetaReadyFlags = new ConcurrentHashMap<>();
            //
            if (tabledMetaMap == null) tabledMetaMap = (Map<String, Set<CacheMeta>>) metaCache.get("tabled_meta");
            if (tabledMetaMap == null) tabledMetaMap = new ConcurrentHashMap<>();

            //
            if (joinedTableFieldsCache == null)
                joinedTableFieldsCache = (Map<String, Map<String, Map<String, String>>>) metaCache.get("joined_table_fields");
            if (joinedTableFieldsCache == null) joinedTableFieldsCache = new ConcurrentHashMap<>();
            //
            if (joinedTablePksCache == null)
                joinedTablePksCache = (Map<String, Map<String, Map<String, String>>>) metaCache.get("joined_table_pks");
            if (joinedTablePksCache == null) joinedTablePksCache = new ConcurrentHashMap<>();
//        }
    }

    /**
     * 需要在后期解决多节点相互覆盖的问题
     * */
    private void saveMetas() {
            if(dataCacheManager==null) return;
//        SimpleTaskManager.doParallelTask(new Runnable() {
//            @Override
//            public void run() {
                DoubleCache metaCache=dataCacheManager.getMetaCache();
                // 此处可能有并发冲突，
                try {
                    if (isMetaReadyFlags != null) metaCache.put("meta_ready_flag", isMetaReadyFlags);
                    if (tabledMetaMap != null) metaCache.put("tabled_meta", tabledMetaMap);
                    if (joinedTableFieldsCache != null) metaCache.put("joined_table_fields", joinedTableFieldsCache);
                    if (joinedTablePksCache != null) metaCache.put("joined_table_pks", joinedTablePksCache);
                } catch (Exception e) {
                    SimpleTaskManager.doParallelTask(new Runnable() {
                        @Override
                        public void run() {
                            saveMetas();
                        }
                    },500);
                }
//        });
    }

    /**
     * 实体关系预构建
     * */
    public PreBuildResult preBuild(DAO dao,Collection<? extends Entity> pos, PropertyRoute route) {

        long t0=System.currentTimeMillis();

        this.dataCacheManager=dao.getDataCacheManager();

        PreBuildResult result=new PreBuildResult();

        if(!route.isCachePropertyData()) {
            return result;
        }

        Set<String> idsFromCache=IDS_FROM_CACHE.get();
        if(idsFromCache==null) {
            idsFromCache=new HashSet<>();
            IDS_FROM_CACHE.set(idsFromCache);
        }

        initMetas(dao.getDataCacheManager());

        Boolean isMetaReady= isMetaReadyFlags.get(route.getKey());
        if(isMetaReady==null || isMetaReady ==false) {
            return result;
        }

        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey = null;
        String dataKey = null;
        List cachedValue;
        Collection builds=new ArrayList<>();
        Collection targets=new ArrayList<>();

        DoubleCache<String,Object> cache=dao.getDataCacheManager().getEntityCache(route.getMasterPoType());
        long ct=0;
        for (Entity po : pos) {
            metaKey= buildMetaKey(route.getProperty(),tm,po);
            dataKey=route.getMasterPoType().getName()+":"+metaKey;
            long t2=System.currentTimeMillis();
            cachedValue=(List) cache.get(dataKey);
            long t4=System.currentTimeMillis();
            ct=ct+(t4-t2);
            if(cachedValue!=null) {

                if(route.getAfter()!=null) {
                    // 缓存构建无法获得m值
                    try {
                        cachedValue = route.getAfter().process(po, cachedValue, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.printStackTrace();
                    }
                }

                if(route.isList()) {
                    BeanUtil.setFieldValue(po,route.getProperty(),cachedValue);
                } else {
                    if(!cachedValue.isEmpty()) {
                        BeanUtil.setFieldValue(po, route.getProperty(), cachedValue.get(0));
                    }
                }
                builds.add(po);
                targets.addAll(cachedValue);
                // 测试用
                idsFromCache.add(route.getMasterPoType().getSimpleName()+"."+route.getProperty()+"$"+route.getSlavePoType().getSimpleName()+":"+BeanUtil.getFieldValue(po,"id",String.class));
            }



        }

        result.setBuilds(builds);
        result.setTargets(targets);

        long t1=System.currentTimeMillis();

        System.err.println("cost= "+(t1-t0) +" ct ="+ct+" , size = "+pos.size());

        return result;
    }

    public void invalidJoinCache(CacheInvalidEventType eventType,DataCacheManager dcm, String table, Entity valueBefore, Entity valueAfter) {
        long t0=System.currentTimeMillis();

        initMetas(dcm);

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

        Set<CacheMeta> metas=this.tabledMetaMap.get(table);
        if(metas==null) {
            this.tabledMetaMap = null;
            initMetas(dcm);
        }
        metas=this.tabledMetaMap.get(table);

        if(metas==null || metas.isEmpty()) return;
        // 搜集失效单元
        List<CacheMeta> all=new ArrayList<>(metas);
        List<CacheMeta> rms=new ArrayList<>();
        for (CacheMeta meta : all) {
            if(willInvalid(eventType,meta,table,clz,valueBefore,valueAfter)) {
                rms.add(meta);
            }
        }
        // 移除失效单元
        for (CacheMeta meta : rms) {
            DoubleCache ch=dcm.getEntityCache(meta.getMasterType());
            if(ch!=null) {
                ch.remove(meta.getValueCacheKey(), false);
            }
            CacheMetaManager.instance().remove(meta,table);
        }
        long t1=System.currentTimeMillis();
        System.err.println("invalidJoinCache cost "+(t1-t0)+" , remove "+rms.size());
    }

    private void remove(CacheMeta meta,String table) {
        Set<CacheMeta> set=tabledMetaMap.get(table);
        if(set!=null) {
            set.remove(meta);
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
