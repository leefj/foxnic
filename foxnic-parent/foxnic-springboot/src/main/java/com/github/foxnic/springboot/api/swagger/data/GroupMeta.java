package com.github.foxnic.springboot.api.swagger.data;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupMeta {

    private static Map<String,GroupMeta> GROUP_META_MAP=new HashMap<>();

    public static GroupMeta get(String group) {
        GroupMeta groupMeta=GROUP_META_MAP.get(group);
        if(groupMeta==null) {
            groupMeta=new GroupMeta(group);
            GROUP_META_MAP.put(group, groupMeta);
        }
        return groupMeta;
    }


    private String group;
    private Map<Class, Set<String>> ctrlPathsMap=new HashMap<>();
    private Map<String,Class> controllerFileMap=new HashMap<>();
    private Map<String,Long> controllerFileLastModifiedMap=new HashMap<>();

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

    /**
     * 获得修改过的控制器类型清单
     * */
    public Set<Class> getModifiedControllers() {
        Set<Class> controllers=new HashSet<>();
        File f=null;
        for (Map.Entry<String, Long> e : controllerFileLastModifiedMap.entrySet()) {
            f=new File(e.getKey());
            if(f.lastModified()!=e.getValue()) {
                controllers.add(controllerFileMap.get(e.getKey()));
            }
        }
        return controllers;
    }





}
