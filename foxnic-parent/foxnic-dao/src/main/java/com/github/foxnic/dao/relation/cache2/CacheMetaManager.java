package com.github.foxnic.dao.relation.cache2;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.entity.Entity;
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

    private CacheMetaManager() { }

    private static Map<Class, LocalCache<String, CacheMeta>> TYPED_META_MAP =new HashMap<>();
    private Map<String, Set<CacheMeta>> TABLED_META_MAP=new HashMap<>();


    private LocalCache<String, CacheMeta> getTypeMeta(PropertyRoute route) {
        LocalCache<String, CacheMeta> typeMeta= TYPED_META_MAP.get(route.getMasterPoType());
        if(typeMeta==null) {
            typeMeta=new LocalCache<>();
            TYPED_META_MAP.put(route.getMasterPoType(),typeMeta);
        }
        return typeMeta;
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
                for (Rcd rcd : rcds) {
                    Object val=rcd.getValue(field.getValue());
                    set.add(val);
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
                for (Rcd rcd : rcds) {
                    Object val=rcd.getValue(field.getValue());
                    set.add(val);
                }
            }
        }


        // 生成 CacheMeta
        LocalCache<String, CacheMeta> typeMeta = getTypeMeta(route);

        // 设置主键
        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey= CacheMeta.buildMetaKey(route.getProperty(),tm,(Entity) owner);

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

    }


    /**
     * 实体关系预构建
     * */
    public Collection<? extends Entity> preBuild(DAO dao,Collection pos, PropertyRoute route) {
        if(System.currentTimeMillis()>0) {
            return new ArrayList<>();
        }
        LocalCache<String, CacheMeta> typeMeta = getTypeMeta(route);
        DBTableMeta tm=dao.getTableMeta(route.getMasterTable().name());
        String metaKey = null;
        String dataKey = null;
        List cachedValue;
        Collection built=new ArrayList<>();
        for (Object po : pos) {
            metaKey= CacheMeta.buildMetaKey(route.getProperty(),tm,(Entity) po);
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
}
