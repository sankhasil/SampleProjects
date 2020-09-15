package com.base.repository;

import java.io.Serializable;
import java.util.List;

import com.google.gson.JsonObject;

public interface IRepository<T> {
	
	List<T> findAll();
    Object findById(String id);
    <S extends T> S save(S object);

    List<T> search(String column, Object value);
    List<T> search(JsonObject object);

    void  deleteById(Serializable objId);

    void delete(T obj);
    void setRepoClass(Class clazz);
    Object update(T object,String id);
    

}
