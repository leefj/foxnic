package com.github.foxnic.dao.entity;

import java.util.List;

import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.spec.DAO;

public interface ISuperService<E> {
	
	DAO dao();
	String table();
	List<E> queryEntities(E sample);
	E queryEntity(E sample);
	PagedList<E> queryPagedEntities(E sample,int pageSize,int pageIndex);
	boolean insertEntity(E entity);
	boolean updateEntity(E entity , SaveMode mode);
	boolean checkExists(E entity,String field);
	<T> boolean deleteByIdsPhysical(List<T> ids);
	<T> boolean deleteByIdsLogical(List<T> ids);
}
