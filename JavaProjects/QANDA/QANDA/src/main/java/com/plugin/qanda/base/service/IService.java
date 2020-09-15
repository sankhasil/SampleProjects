/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plugin.qanda.base.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;

/**
 *
 * @author Sankha
 */
public interface IService<T>  {

    List<T> findAll();

    <S extends T> S findById(String id);
    void setDao(CrudRepository dao);

    <S extends T> S save(S object);

    <S extends T> List<S> saveAll(List<S> object);

    List<T> goToPage(int pageNo);

    List<T> search(String column, Object value, Object value2);

    List<T> search(JsonObject object);

    T create(T object);

    T update(T object,String id);

    T patch(T objectTopatch,String id) ;

    void  deleteById(Serializable objId);

    void delete(T obj);
    
    void validateEntity(T object) ;

    void setBusinessClass(Class cls);
    
    void storeFile(MultipartFile file);

	List<T> searchByField(String field, Object value);
}
