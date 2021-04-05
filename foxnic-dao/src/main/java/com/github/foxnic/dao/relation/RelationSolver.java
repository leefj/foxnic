package com.github.foxnic.dao.relation;

import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.spec.DAO;

import java.util.Collection;
import java.util.List;

public class RelationSolver {

    private DAO dao;
    private RelationManager relationManager;

    public RelationSolver(DAO dao)  {
        this.dao=dao;
        this.relationManager=dao.getRelationManager();
    }




    public <E extends Entity,T extends Entity> void join(E po, Class<T> targetType) {
        if(po==null) return;
        List<PropertyRoute> prs = this.relationManager.findProperties(po.getClass(),targetType);
        if(prs==null || prs.isEmpty()) return;

        //构建语句

    }

    public <E extends Entity,T extends Entity> void join(Collection<E> pos, Class<T> targetType) {
        if(pos==null || pos.isEmpty()) return;
        E sample=null;
        for (E e:pos) {
            if(e!=null) sample=e;
        }
        if(sample==null) return;
        List<PropertyRoute> prs = this.relationManager.findProperties(sample.getClass(),targetType);
        if(prs==null || prs.isEmpty()) return;

        //构建语句
    }

}
