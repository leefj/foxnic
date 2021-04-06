package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.entity.CollectorUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.In;
import com.github.foxnic.sql.expr.Select;
import com.github.foxnic.sql.expr.Where;

public class RelationSolver {

    private DAO dao;
    private RelationManager relationManager;

    public RelationSolver(DAO dao)  {
        this.dao=dao;
        this.relationManager=dao.getRelationManager();
    }




 

    
    public <E extends Entity,T extends Entity> Map<String,JoinResult> join(Collection<E> pos, Class<T> targetType) {
        if(pos==null || pos.isEmpty()) return null;
        Class<E> poType = getPoType(pos);
        List<PropertyRoute> prs = this.relationManager.findProperties(poType,targetType);
        if(prs==null || prs.isEmpty()) return null;

        Map<String,JoinResult> map=new HashMap<>();
        for (PropertyRoute route : prs) {
        	JoinResult jr=this.join(poType,pos,route,targetType);
        	map.put(route.getProperty(), jr);
		}
        return map;
    }




	private <E extends Entity> Class<E> getPoType(Collection<E> pos) {
		E sample=null;
        for (E e:pos) {
            if(e!=null) sample=e;
        }
        if(sample==null) return null;
        Class<E> poType=(Class<E>)sample.getClass();
		return poType;
	}

    
	private <E extends Entity,T extends Entity> JoinResult join(Class<E> poType, Collection<E> pos,PropertyRoute route,Class<T> targetType) {
 
		if(pos==null || pos.size()==0) {
			return new JoinResult();
		}
		
		String poTable=EntityUtil.getAnnotationTable(poType);
		String targetTable=EntityUtil.getAnnotationTable(targetType);
		
		//最后一个 Join 的 target 与 targetTable 一致
		List<Join> joinPath = this.dao.getRelationManager().findJoinPath(poTable,targetTable);
		if(joinPath.isEmpty()) {
			throw new RuntimeException("未配置关联关系");
		}
		
		Join firstJoin=joinPath.get(0);
		Join lastJoin=joinPath.remove(joinPath.size()-1);
 
		int i=0;
		
		String sourceAliasName=null;
		String targetAliasName="t_"+i;
		Select select=new Select();
		select.from(firstJoin.getTargetTable(),targetAliasName);
		select.select(targetAliasName+".*");
		
		Expr expr=new Expr();
		Join prev=null;
		for (Join join : joinPath) {
			i++;
			sourceAliasName="t_"+i;
			//join 一个表
			Expr joinExpr=new Expr("<$break.br/>"+join.getJoinType().getJoinSQL()+" "+join.getSourceTable()+" "+sourceAliasName+" on ");
			//循环拼接 Join 的条件
			List<String> basicCdrs=new ArrayList<>();
			for (int j = 0; j < join.getSourceTableFields().size(); j++) {
				String cdr=sourceAliasName+"."+join.getSourceTableFields().get(j)+" = "+targetAliasName+"."+join.getTargetTableFields().get(j);
				basicCdrs.add(cdr);
			}
			joinExpr.append(StringUtil.join(basicCdrs," and "));
			expr.append(joinExpr);
			targetAliasName="t_"+i;
			prev=join;
		}
		
		i=0;
		String[] groupFields=new String[lastJoin.getTargetTableFields().size()];
		for (String f : lastJoin.getTargetTableFields()) {
			groupFields[i]="join_f"+i;
			select.select(targetAliasName+"."+f,groupFields[i]);
			i++;
		}
		
		Expr selcctExpr=new Expr(select.getListParameterSQL(),select.getListParameters());
		expr=selcctExpr.append(expr);
		
		In in=null;
		if(lastJoin.getTargetTableFields().size()==1) {
			Object[] values=BeanUtil.getFieldValueArray(pos, lastJoin.getSourceTableFields().get(0), Object.class);
			in=new In(lastJoin.getTargetTableFields().get(0), values);
		} else {
			
		}
				
		Where wh=new Where();
		wh.and(in);
		expr.append("<$break.br/>");
		expr.append(wh);
		
		expr=new Expr(expr.getListParameterSQL().replace("<$break.br/>", "\n"),expr.getListParameters());
		
		
		
		RcdSet targets=dao.query( expr);
		
//		Map<Object,List<Rcd>> gs=targets.getGroupedMap(groupFields);
		String[] keyParts=new String[groupFields.length];
		Map<Object,List<Rcd>> gs=CollectorUtil.groupBy(targets.getRcdList(), r -> {
			for (int j = 0; j < keyParts.length; j++) {
				String f = groupFields[j];
				keyParts[j]=r.getString(f);
			}
			return StringUtil.join(keyParts);
		});
		
		
		//填充关联数据
		pos.forEach(p->{
			for (int j = 0; j < keyParts.length; j++) {
				String f = lastJoin.getSourceTableFields().get(j);
				keyParts[j]=BeanUtil.getFieldValue(p,f,String.class);
			}
			List<Rcd> tcds=gs.get(StringUtil.join(keyParts));
			Set<T> list=new HashSet<>();
			if(tcds!=null) {
				for (Rcd r : tcds) {
					list.add(r.toEntity(targetType));
				}
				if(route.getAfter()!=null) {
					list=route.getAfter().process(list);
				}
			}
			BeanUtil.setFieldValue(p, route.getProperty(), list);
		});
		JoinResult jr=new JoinResult();
		return jr;
	}




	public <E extends Entity,T extends Entity> Map<String, JoinResult> join(Collection<E> pos, String[] properties) {
		if(pos==null || pos.isEmpty()) return null;
		Class<E> poType = getPoType(pos);
		
		 Map<String,JoinResult> map=new HashMap<>();
	        
		
		for (String prop : properties) {
			PropertyRoute pr=dao.getRelationManager().findProperties(poType,prop);
			if(pr==null) {
				throw new IllegalArgumentException(poType.getSimpleName()+"."+prop+" 未配置");
			}
			JoinResult jr=this.join(poType,pos,pr,pr.getTargetPoType());
			map.put(pr.getProperty(), jr);
		}
		
		return map;
	}

}
