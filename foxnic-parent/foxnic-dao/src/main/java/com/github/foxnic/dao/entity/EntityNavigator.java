package com.github.foxnic.dao.entity;

import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.spec.DAO;

import java.util.*;

public class EntityNavigator {

    private static class Node {
        private String parentPath;
        private String prop;
        private List<Node> subs=new ArrayList<>();
        private Set<String> subProperties=new HashSet<>();
        public Node(String prop){
            this.prop=prop;
            this.parentPath ="";
        }

        public String[] getSubProperties() {
            return subProperties.toArray(new String[0]);
        }

        public boolean addSubNode(Node node) {
            if(subProperties.contains(node.prop)) return false;
            subProperties.add(node.prop);
            subs.add(node);
            return true;
        }
    }

    private Collection<Entity> entities;
    private DAO dao;
    private Node root;
    private int depth=-1;
    private Map<String,Node> nodes=new HashMap<>();

    public EntityNavigator(DAO dao, Collection<Entity> entities){
        this.dao=dao;
        this.entities=entities;
        this.root=new Node("root");
    }
    /**
     * 指定填充的数据路径
     * */
    public EntityNavigator with(String... prop) {
        String p=null;
        String par=null;
        for (int i = 0; i < prop.length; i++) {
            p=prop[i];
            Node node=new Node(p);
            boolean added=false;
            if(i==0) { //根节点
                added=this.root.addSubNode(node);
                node.parentPath =root.prop;
            } else {
                Node parent=this.nodes.get((i-1)+":"+par);
                added=parent.addSubNode(node);
                node.parentPath =parent.parentPath +"."+parent.prop;
            }
            if(added) {
                this.nodes.put(i + ":" + node.prop, node);
            }
            par=p;
        }
        if(depth< prop.length) depth= prop.length;
        return this;
    }


    private Map<String,List<Entity>> data=new HashMap<>();

    /**
     * 按照已经配置的路径，执行数据填充
     * */
    public void execute() {
        //join根节点
        Collection<Entity> target=this.entities;
        Map<String, JoinResult<Entity,Entity>> resultMap = dao.join(target,this.root.getSubProperties());
        List<Entity> resultList=null;
        for (Map.Entry<String, JoinResult<Entity, Entity>> entry : resultMap.entrySet()) {
            List<Entity> result=(List<Entity>)entry.getValue().getTargetList();
            this.data.put(this.root.prop+"."+entry.getKey(),result);
        }
        //join后续层级的节点
        for (int i = 0; i < depth; i++) {
            for (Map.Entry<String, Node> e : nodes.entrySet()) {
                if(e.getKey().startsWith(i+":")) {
                    target=data.get(e.getValue().parentPath +"."+e.getValue().prop);
                    if(target==null || target.isEmpty()) continue;
                    if(e.getValue().getSubProperties().length==0) continue;
                    resultMap=dao.join(target,e.getValue().getSubProperties());
                    for (Map.Entry<String, JoinResult<Entity, Entity>> entry : resultMap.entrySet()) {
                        List<Entity> result=(List<Entity>)entry.getValue().getTargetList();
                        this.data.put(e.getValue().parentPath +"."+e.getValue().prop+"."+entry.getKey(),result);
                    }
                }
            }
        }
    }


}
