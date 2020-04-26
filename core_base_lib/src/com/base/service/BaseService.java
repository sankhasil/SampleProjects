/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.base.service;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.multipart.MultipartFile;

import com.base.enums.HttpHeaders;
import com.base.exception.BaseRuntimeException;
import com.base.model.BaseModel;
import com.base.repository.BaseRepository;
import com.base.utils.HttpUtils;
import com.google.gson.JsonObject;

/**
 * @author Sankha
 */
public class BaseService<T> implements IService<T> {

	Logger logger = LoggerFactory.getLogger(BaseService.class);
	protected CrudRepository<T, Serializable> dao;// id parameter should be generic id extents serializable	

	protected Class busClass;
	protected BaseRepository baseDao;
//	protected BaseDocumentRepository baseDocDao;
	public BaseService() {
//		logger.info("---------initialising @ BaseService----" + getClass().getSimpleName());
	}

	public BaseService(CrudRepository dao) {
		this.dao = dao;
		
	}
//	public BaseService(GridFsTemplate gridFsTemplate) {
//		this.baseDocDao = new BaseDocumentRepository(gridFsTemplate);
//		
//	}
//	public BaseService(CrudRepository dao,GridFsTemplate gridFsTemplate) {
//		this.dao = dao;
//		this.baseDocDao = new BaseDocumentRepository(gridFsTemplate);
//		
//	}

//	public BaseService(CrudRepository dao, MongoTemplate mongoTemplate,GridFsTemplate gridFsTemplate) {
//		this.dao = dao;
//		this.baseDao = new BaseRepository<>(mongoTemplate);
//		this.baseDocDao = new BaseDocumentRepository(gridFsTemplate);
//	}
	public BaseService(CrudRepository dao, MongoTemplate mongoTemplate) {
		this.dao = dao;
		this.baseDao = new BaseRepository<>(mongoTemplate);
		
	}


	
	public List<T> findAll() {

		List<T> l = new ArrayList<>();
		Iterable<T> all = dao.findAll();
		all.forEach(e -> l.add(e));
		return l;
	}

	@Override
	public List<T> searchByField(String field, Object value) {
		if(StringUtils.isNotBlank(field))
			return baseDao.search(field, value);
		return null;
	}

	public List<T> goToPage(int pageNo) {
		List<T> list = goToPage(pageNo, 100);
		return list;
	}

	public List<T> goToPage(int pageNo, int size) {
		// FIXME: Write the query for specific page.
//        List<T> list =dao.goToPage(pageNo,size);
		return null;
	}

	public Page<T> searchPageable(String column, Object value, Pageable pageable) {
//        Page<T> page= dao.searchPageable(column, value, pageable);
		return null;
	}

	public List<T> search(String column, Object value, Object value2) {
		// FIXME : Write the search code
		return null;
	}


	public List<T> search(JsonObject object) {
		return baseDao.search(object);
	}

	public T findById(String id) {
		if (baseDao != null)
			return (T) baseDao.findById(id);
		else {

			Optional<T> byId = dao.findById(id);
			if (byId.isPresent())
				return byId.get();
			return null;
		}
	}

	public void setDao(CrudRepository dao) {
		this.dao = dao;
	}

	/**
	 * @param baseDao the baseDao to set
	 */
	public void setBaseDao(BaseRepository baseDao) {
		this.baseDao = baseDao;
	}
	
//	public void setBaseDocDao(BaseDocumentRepository baseDocDao) {
//		this.baseDocDao = baseDocDao;
//	}


	public <S extends T> S save(S object) {
		try {
			StringBuilder objectInString = new StringBuilder();
			validateEntity(object);
			
			setCurrentEpochDateToObject(object);
			if (object instanceof BaseModel) 
				HttpUtils.setHeadersToPayloadForSave(object);
				
			S objectSaved = dao.save(object);
			return objectSaved;
		} catch (Exception e) {
			throw new BaseRuntimeException("persistance error from save", e);
		}
	}

	private <S extends T> void setCurrentEpochDateToObject(S object) {
		if (object instanceof BaseModel) {
			if (((BaseModel) object).getCreatedDateTimeInEpoch() == null) {
				((BaseModel) object).setCreatedDateTimeInEpoch(Instant.now().getEpochSecond());
			}
		} 

	}

	public <S extends T> List<S> saveAll(List<S> object) {
		try {
			List<S> persisted = new ArrayList<>();
			for (S subObject : object) {
				validateEntity(subObject);
				setCurrentEpochDateToObject(subObject);
				if (object instanceof BaseModel)
					HttpUtils.setHeadersToPayloadForSave(object);
				persisted.add(dao.save(subObject));

			}
			return persisted;

		} catch (Exception e) {
			throw new BaseRuntimeException("Persistance error from save", e);
		}
	}

	public T create(T object) {
		if (object instanceof BaseModel) {
			try {
				validateEntity(object);
						HttpUtils.setHeadersToPayloadForSave(object);
						((BaseModel) object)
								.setCreatedBy(HttpUtils.getHeader(HttpHeaders.USER_NAME));
						setCurrentEpochDateToObject(object);
				return dao.save(object);
			} catch (Exception e) {
				throw new BaseRuntimeException("Persistance error from create", e);
			}
		}
		return null;
	}

	public T update(T objectTopatch, String id) {
		T object = null;
		if (baseDao != null) {
			if (objectTopatch instanceof BaseModel) {
				((BaseModel) objectTopatch).setUpdatedBy(HttpUtils.getHeader(HttpHeaders.USER_NAME));
				((BaseModel) objectTopatch).setUpdatedDateTimeInEpoch(Instant.now().getEpochSecond());
			object = (T) baseDao.update(objectTopatch, id);
			}
		}
		return object;
	}

	public T patch(T objectTopatch, String id) {

		if (objectTopatch == null)
			throw new BaseRuntimeException("There should exist  an object already");
		if (StringUtils.isNotBlank(id) && dao.existsById(id) && objectTopatch instanceof BaseModel) {
			T ob = update(objectTopatch, id);
			logger.info("Successfully updated object " + objectTopatch);
			return ob;
		}
		return null;
	}

	public void deleteById(Serializable objId) {
		if (!dao.existsById(objId))
			throw new BaseRuntimeException("There should exist  an object already");
		dao.deleteById(objId);
	}

	public void delete(T objId) {
		if (objId == null)
			throw new BaseRuntimeException("There should exist  an object already");
		dao.delete(objId);
	}

	public void validateEntity(T object) {
		// http://docs.jboss.org/hibernate/validator/4.1/reference/en-US/html/programmaticapi.html#example-constraint-mapping
		// this explains the dynamic validation config machanism
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void setBusinessClass(Class cls) {
		this.busClass = cls;
		if (this.baseDao != null)
			this.baseDao.setClazz(cls);
	}
//	public void storeFile(MultipartFile file) {
//		try {
//		InputStream fileInputStream = file.getInputStream();
//		baseDocDao.storeFile(fileInputStream, file.getName(), file.getContentType());
//		} catch (IOException e) {
//			logger.error(ExceptionUtils.getStackTrace(e));
//		}
//	}

	@Override
	public void storeFile(MultipartFile file) {
		
	}

}
