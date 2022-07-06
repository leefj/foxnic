package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.concurrent.SimpleJoinForkTask;
import com.github.foxnic.commons.log.Logger;
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
    private String tag=DAO.DEFAULT_JOIN_TAG;
    private Map<String,Node> nodes=new HashMap<>();

    public EntityNavigator(DAO dao, Collection<? extends Entity> entities){
        this.dao=dao;
        this.entities=(Collection<Entity>)entities;
        this.root=new Node("root");
    }

    public EntityNavigator tag(String tag) {
        this.tag=tag;
        return this;
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
        if(dao==null || this.entities==null) return;
        long t=System.currentTimeMillis();
        //join根节点
        Collection<Entity> target=this.entities;
        if(target==null || target.isEmpty()) return;
        Map<String, JoinResult> resultMap = dao.join(tag,target,this.root.getSubProperties());
        if(resultMap==null) return;
        List<Entity> resultList=null;
        for (Map.Entry<String, JoinResult> entry : resultMap.entrySet()) {
            if(entry.getValue()==null) continue;
            List<Entity> result=(List<Entity>)entry.getValue().getTargetList();
            this.data.put(this.root.prop+"."+entry.getKey(),result);
        }
        //join 后续层级的节点,按深度逐级 join
        for (int i = 0; i < depth; i++) {

//            for (Map.Entry<String, Node> e : nodes.entrySet()) {
//                if(!e.getKey().startsWith(i+":")) continue;
//
//                target=data.get(e.getValue().parentPath +"."+e.getValue().prop);
//                if(target==null || target.isEmpty()) continue;
//                if(e.getValue().getSubProperties().length==0) continue;
//                resultMap=dao.join(target,e.getValue().getSubProperties());
//                for (Map.Entry<String, JoinResult> entry : resultMap.entrySet()) {
//                    List<Entity> result=(List<Entity>)entry.getValue().getTargetList();
//                    this.data.put(e.getValue().parentPath +"."+e.getValue().prop+"."+entry.getKey(),result);
//                }
//            }
            List<SubUnit> units=new ArrayList<>();
            //采集任务
            for (Map.Entry<String, Node> e : nodes.entrySet()) {
                if(!e.getKey().startsWith(i+":")) continue;

                //取属性所在的数据对象
                target=data.get(e.getValue().parentPath +"."+e.getValue().prop);
                if(target==null || target.isEmpty()) continue;
                if(e.getValue().getSubProperties().length==0) continue;
                SubUnit unit=new SubUnit();
                unit.target=target;
                unit.props=e.getValue().getSubProperties();
                unit.parentPath=e.getValue().parentPath;
                unit.property=e.getValue().prop;
                units.add(unit);
            }

            //并行执行
            SimpleJoinForkTask<SubUnit,SubUnit> task=new SimpleJoinForkTask<>(units,1);
            List<SubUnit> allr= task.execute(els->{
                for (SubUnit el : els) {
                    Map<String, JoinResult> m=dao.join(tag,el.target,el.props);
                    el.result=m;
                }
                return els;
            });

            //结果处理
            for (SubUnit unit : allr) {
                for (Map.Entry<String, JoinResult> entry : unit.result.entrySet()) {
                    List<Entity> result=(List<Entity>)entry.getValue().getTargetList();
                    this.data.put(unit.parentPath +"."+unit.property+"."+entry.getKey(),result);
                }
            }

        }
        t=System.currentTimeMillis()-t;
        if(dao.isPrintSQL()) {
            Logger.info("fill with cost " + t + "ms");
        }
    }

    private static class SubUnit {
        private Collection<Entity> target;
        private String[] props;
        private String parentPath;
        private String property;
        private Map<String, JoinResult> result;
    }

}
