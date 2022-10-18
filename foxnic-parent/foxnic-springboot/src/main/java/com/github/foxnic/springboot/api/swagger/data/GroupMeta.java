package com.github.foxnic.springboot.api.swagger.data;

import com.github.foxnic.commons.bean.BeanUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import springfox.documentation.spring.web.json.Json;

public class GroupMeta {

    public static  enum ProcessMode {
        INIT,FULL_CACHE,PART_CACHE;
    }

    private static Map<String,GroupMeta> GROUP_META_MAP=new HashMap<>();

    public static GroupMeta get(String group) {
        GroupMeta groupMeta=GROUP_META_MAP.get(group);
        if(groupMeta==null) {
            groupMeta=new GroupMeta(group);
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

    public GroupMeta(String group) {
        this.group=group;
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
