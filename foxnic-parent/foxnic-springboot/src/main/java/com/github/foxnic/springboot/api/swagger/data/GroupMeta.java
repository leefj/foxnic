package com.github.foxnic.springboot.api.swagger.data;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.meta.DBTable;
import com.github.foxnic.sql.treaty.DBTreaty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.*;

import springfox.documentation.spring.web.json.Json;

public class GroupMeta {

    /**
     * 需要从  ModuleContext  @ 135 复制
     * */
    private static final Set<String> DEFAULT_VO_PROPS= ArrayUtil.asSet("pageIndex","pageSize","searchField","fuzzyField","searchValue","dirtyFields","sortField","sortType");

    public DBTableMeta getTableMeta(DBTable table) {
        return this.dao.getTableMeta(table.name());
    }

    public static  enum ProcessMode {
        INIT,FULL_CACHE,PART_CACHE;
    }

    private static Map<String,GroupMeta> GROUP_META_MAP=new HashMap<>();

    public static GroupMeta get(String group) {

        GroupMeta groupMeta=GROUP_META_MAP.get(group);
        if(groupMeta==null) {
            DAO dao= SpringUtil.getBean(DAO.class);
            DBTreaty dbTreaty=null;
            if(dao!=null) {
                dbTreaty=dao.getDBTreaty();
            }
            groupMeta=new GroupMeta(group,dao,dbTreaty);
            GROUP_META_MAP.put(group, groupMeta);
        }
        return groupMeta;
    }


    private ProcessMode mode;
    private String group;
    private ResponseEntity responseEntity;
    private Map<String, String> modelNameMapping;
    private Map<Class, Set<String>> ctrlPathsMap=new HashMap<>();
    private Map<String,Class> controllerFileMap=new HashMap<>();
    private Map<String,Long> controllerFileLastModifiedMap=new HashMap<>();
    private Map<String,Class> modelFileMap =new HashMap<>();
    private Map<String,Long> modelFileLastModifiedMap=new HashMap<>();

    private DBTreaty dbTreaty;
    private DAO dao;
    private GroupMeta(String group,DAO dao,DBTreaty dbTreaty) {
        this.group=group;
        this.dao=dao;
        this.dbTreaty=dbTreaty;
    }

    public void registerControllerPath(Class controller,File controllerFile,String path) {
        controllerFileMap.put(controllerFile.getAbsolutePath(),controller);
        Set<String> paths=ctrlPathsMap.get(controller);
        if(paths==null) {
            paths = new HashSet<>();
            ctrlPathsMap.put(controller,paths);
        }
        paths.add(path);
        controllerFileLastModifiedMap.put(controllerFile.getAbsolutePath(),controllerFile.lastModified());
    }

    public void registerModel(Class model,File modelFile) {
        this.modelFileMap.put(modelFile.getAbsolutePath(),model);
        this.modelFileLastModifiedMap.put(modelFile.getAbsolutePath(),modelFile.lastModified());
    }

    public boolean isDBTreatyProperty(String prop) {
        if(dbTreaty==null) return false;
        prop=BeanNameUtil.instance().depart(prop);
        return dbTreaty.isDBTreatyFiled(prop,true);
    }

    public boolean isPrimaryKey(String table,String prop) {
        if(StringUtil.isBlank(table)) return false;
        DBTableMeta tm=dao.getTableMeta(table);
        if(tm==null) return false;
        prop=BeanNameUtil.instance().depart(prop);
        return tm.isPK(prop);
    }

    public boolean isDefaultVoProperty(String table, String prop) {
        boolean flag=DEFAULT_VO_PROPS.contains(prop);
        if(!flag) {
            DBTableMeta tm=dao.getTableMeta(table);
            if(tm==null) return false;
            if(tm.getPKColumnCount()==1) {
                String pks=tm.getPKColumns().get(0).getColumnVarName()+"s";
                if(pks.equals(prop)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    private Set<Class> modifiedControllers=null;
    /**
     * 获得修改过的控制器类型清单
     * */
    public Set<Class> getModifiedControllers() {
        if(modifiedControllers!=null) return modifiedControllers;
        modifiedControllers=new HashSet<>();
        File f=null;
        for (Map.Entry<String, Long> e : controllerFileLastModifiedMap.entrySet()) {
            f=new File(e.getKey());
            if(f.lastModified()!=e.getValue()) {
                modifiedControllers.add(controllerFileMap.get(e.getKey()));
            }
        }
        return modifiedControllers;
    }

    private Set<Class> modifiedModels=null;
    public Set<Class> getModifiedModels() {
        if(modifiedModels!=null) return modifiedModels;
        modifiedModels=new HashSet<>();
        File f = null;
        for (Map.Entry<String, Long> e : modelFileLastModifiedMap.entrySet()) {
            f=new File(e.getKey());
            if(f.lastModified()!=e.getValue()) {
                modifiedModels.add(modelFileMap.get(e.getKey()));
            }
        }
        return modifiedModels;
    }


    public ResponseEntity getResponseEntity() {
        if(responseEntity==null) return null;
        Json body=(Json)responseEntity.getBody();
        if(body==null) return null;
        Json newBody=new Json(body.value());
        ResponseEntity entity=new ResponseEntity(newBody,HttpStatus.OK);
        return entity;
    }

    public void setResponseEntity(ResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }

    public String getGroup() {
        return group;
    }

    public Map<String, String> getModelNameMapping() {
        return modelNameMapping;
    }

    public void setModelNameMapping(Map<String, String> modelNameMapping) {
        this.modelNameMapping = modelNameMapping;
    }

    public DBTable getTable(Class entityType) {
        DBTable table= null;
        while(table==null && entityType!=null) {
            table= EntityUtil.getDBTable(entityType);
            if(table==null) {
                entityType = entityType.getSuperclass();
            }
        }
        return table;
    }

    public void reset() {
        modifiedControllers = null;
        modifiedModels = null;
    }

    public ProcessMode getMode() {
        return mode;
    }

    public void setMode(ProcessMode mode) {
        this.mode = mode;
    }
}
