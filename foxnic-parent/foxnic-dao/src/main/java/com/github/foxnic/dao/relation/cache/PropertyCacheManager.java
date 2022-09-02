package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.log.PerformanceLogger;
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

public class PropertyCacheManager {

    public static final Set<String> IDS_FROM_CACHE=new HashSet<>();

    private static final PropertyCacheManager instance =new PropertyCacheManager();

    public static PropertyCacheManager instance() {
        return instance;
    }

    private DataCacheManager dataCacheManager=null;


    /**
     * 重置缓存
     * */
    public void reset() {
        if(dataCacheManager!=null) {
            dataCacheManager.clearAllCachedData();
        }
        joinedTableFieldsCache=null;
        joinedTablePksCache=null;
        isMetaReadyFlags=null;
        if(cacheMetaManager!=null) {
            cacheMetaManager.reset();
        }
    }


    private PropertyCacheManager() {
        (new SimpleTaskManager()).doIntervalTask(new Runnable() {
            @Override
            public void run() {
                    saveMetas();
            }
        },1000);
    }

//    private Map<Class, Map<String, CacheMeta>> typedMetaMap = new HashMap<>();
//    private Map<String, Set<CacheMeta>> tabledMetaMap = null;
    private CacheMetaManager cacheMetaManager=null;


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

        PerformanceLogger logger=new PerformanceLogger(true);
        logger.collect("S1");
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

        logger.collect("S2");
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

        logger.collect("S3");


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

        logger.collect("S4");

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

        logger.collect("S5");


        // 生成 CacheMeta
//        Map<String, CacheMeta> typeMeta = getTypeMeta(route);

        // 设置主键
        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey= buildMetaKey(route.getProperty(),tm,(Entity) owner);

        // 构建按数据表索引
        CacheMeta cacheMeta = new CacheMeta(IDGenerator.getSnowflakeId(),route.getMasterPoType(), route.getMasterTable().name(),route.getProperty(), joinedTablePks, joinedTableFields);

        // 填充数据
        cacheMeta.setValues(joinedTablePkValues,joinedTableFieldValues);

        List<DBColumnMeta> pks=tm.getPKColumns();
        for (DBColumnMeta pk : pks) {
            cacheMeta.setOwnerId(pk.getColumn(),BeanUtil.getFieldValue(owner,pk.getColumn()));
        }
        logger.collect("S6");



        // 主表
        String masterTable=route.getMasterTable().name().toLowerCase();
        cacheMetaManager.addUnit(masterTable,cacheMeta.getId());
        // 中间表
        for (String otherTable : joinedTableFields.keySet()) {
            cacheMetaManager.addUnit(otherTable,cacheMeta.getId());
        }
        cacheMetaManager.addCacheMeta(cacheMeta);

        // 缓存 CacheUnit
        metaKey=cacheMeta.getMetaKey();
        // 缓存属性数据
        String dataKey=route.getMasterPoType().getName()+":"+metaKey;

        logger.collect("S7");

        List valuesToCahce=new ArrayList(value.size());
        Object e=null;
        for (int i = 0; i < value.size(); i++) {
            e=value.get(i);
            if(e==null) {
                valuesToCahce.add(e);
                continue;
            }
            if(e instanceof Entity) {
                valuesToCahce.add(((Entity)e).duplicate(false));
            } else {
                throw new IllegalArgumentException("仅支持 Entity 类型");
            }
        }

        logger.collect("S8");

        cache.put(dataKey,valuesToCahce);
        cacheMeta.setValueCacheKey(dataKey);
        isMetaReadyFlags.put(route.getKey(),true);

        logger.collect("S9");

//        long tf=System.currentTimeMillis();


//        System.err.println("cache save ::  "+route.getMasterPoType().getSimpleName()+"."+route.getProperty()+" : cost = "+(tf-t)+" , size = "+value.size());

        logger.info("join cache save");

    }

    private Map<String,Boolean> isMetaReadyFlags = null;

    private void initMetas(DataCacheManager dataCacheManager) {

            DoubleCache metaCache = dataCacheManager.getMetaCache();
            //
            if (isMetaReadyFlags == null) isMetaReadyFlags = (Map<String, Boolean>) metaCache.get("meta_ready_flag");
            if (isMetaReadyFlags == null) isMetaReadyFlags = new ConcurrentHashMap<>();
            //
            if (cacheMetaManager == null) cacheMetaManager = new CacheMetaManager(metaCache);
            //
            if (joinedTableFieldsCache == null)
                joinedTableFieldsCache = (Map<String, Map<String, Map<String, String>>>) metaCache.get("joined_table_fields");
            if (joinedTableFieldsCache == null) joinedTableFieldsCache = new ConcurrentHashMap<>();
            //
            if (joinedTablePksCache == null)
                joinedTablePksCache = (Map<String, Map<String, Map<String, String>>>) metaCache.get("joined_table_pks");
            if (joinedTablePksCache == null) joinedTablePksCache = new ConcurrentHashMap<>();
    }

    /**
     * 需要在后期解决多节点相互覆盖的问题
     * */
    private synchronized void saveMetas() {
            if(dataCacheManager==null) return;
//        SimpleTaskManager.doParallelTask(new Runnable() {
//            @Override
//            public void run() {
                DoubleCache metaCache=dataCacheManager.getMetaCache();
                // 此处可能有并发冲突，
                try {
                    if (isMetaReadyFlags != null) metaCache.put("meta_ready_flag", isMetaReadyFlags);
                    if (cacheMetaManager != null) cacheMetaManager.save();
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
    public PreBuildResult preBuild(String tag,DAO dao,Collection<? extends Entity> pos, PropertyRoute route) {

        PerformanceLogger logger=new PerformanceLogger(true);
        logger.collect("P1");
        this.dataCacheManager=dao.getDataCacheManager();

        PreBuildResult result=new PreBuildResult();

        if(!route.isCachePropertyData()) {
            return result;
        }

        initMetas(dao.getDataCacheManager());
        logger.collect("P2");
        Boolean isMetaReady= isMetaReadyFlags.get(route.getKey());
        if(isMetaReady==null || isMetaReady ==false) {
            return result;
        }
        logger.collect("P3");
        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey = null;
        String dataKey = null;
        List cachedValue;
        Collection builds=new ArrayList<>();
        Collection targets=new ArrayList<>();

        DoubleCache<String,Object> cache=dao.getDataCacheManager().getEntityCache(route.getMasterPoType());

        logger.collect("P4");
        Set<String> dataKeys=new HashSet<>();
        for (Entity po : pos) {
            metaKey = buildMetaKey(route.getProperty(), tm, po);
            dataKey = route.getMasterPoType().getName() + ":" + metaKey;
            dataKeys.add(dataKey);
        }
        Map<String,Object> all=cache.getAll(dataKeys);
        logger.collect("P5");

        logger.collect("P6");
        for (Entity po : pos) {
            metaKey= buildMetaKey(route.getProperty(),tm,po);
            dataKey=route.getMasterPoType().getName()+":"+metaKey;

            cachedValue=(List) all.get(dataKey);



            if(cachedValue!=null ) {

                List valuesToReturn=new ArrayList(cachedValue.size());
                Object e = null;
                for (int i = 0; i < cachedValue.size(); i++) {
                    e=cachedValue.get(i);
                    if(e==null) {
                        valuesToReturn.add(e);
                        continue;
                    }
                    if(e instanceof Entity) {
                        valuesToReturn.add(((Entity)e).duplicate(false));
                    } else {
                        throw new IllegalArgumentException("仅支持 Entity 类型");
                    }
                }


                if(route.getAfter()!=null) {
                    // 缓存构建无法获得m值
                    try {
                        cachedValue = route.getAfter().process(tag,po, cachedValue, null);
                    } catch (Exception ex) {
                        Logger.exception("prebuild do after ",ex);
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
                //  设置 owner , 因并发问题，取消改特性
//                for (Object e : cachedValue) {
//                    if(e instanceof  Entity) {
//                        BeanUtil.setFieldValue(e,"$owner",po);
//                    }
//                }
                // 测试用
                IDS_FROM_CACHE.add(route.getMasterPoType().getSimpleName()+"."+route.getProperty()+"$"+route.getSlavePoType().getSimpleName()+":"+BeanUtil.getFieldValue(po,"id",String.class));
            }



        }

        logger.collect("P7");

        result.setBuilds(builds);
        result.setTargets(targets);

//        long t1=System.currentTimeMillis();

//        System.err.println("prebuilt :: "+route.getMasterPoType().getSimpleName()+"."+route.getProperty()+" : cost = "+(t1-t0) +" ; cache fetch time ="+(t4-t2)+" , size = "+pos.size());
        logger.collect("P8");

        logger.info("join cache prebuild");

        return result;
    }

    /**
     * @param editingTable 正在编辑的表名
     * */
    public void invalidJoinCache(CacheInvalidEventType eventType,DataCacheManager dcm, String editingTable, Entity valueBefore, Entity valueAfter) {
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

        clz=EntityContext.getPoType(clz);

        Collection<CacheMeta> metas=this.cacheMetaManager.getCacheMetas(editingTable);

        if(metas==null || metas.isEmpty()) return;
        // 搜集失效单元
        List<CacheMeta> all=new ArrayList<>(metas);
        List<CacheMeta> rms=new ArrayList<>();
        for (CacheMeta meta : all) {
            if(willInvalid(eventType,meta,editingTable,clz,valueBefore,valueAfter)) {
                rms.add(meta);
            }
        }
        // 移除失效单元
        for (CacheMeta meta : rms) {
            DoubleCache ch=dcm.getEntityCache(meta.getMasterType());
            // 移除缓存的数据
            if(ch!=null) {
                ch.remove(meta.getValueCacheKey(), false);
            }
            // 移除缓存的元数据
            this.cacheMetaManager.remove(editingTable,meta);
        }
        long t1=System.currentTimeMillis();
//        System.err.println("invalid join cache :: cost = "+(t1-t0)+" , remove = "+rms.size());
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
