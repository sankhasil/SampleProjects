package com.base.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.base.utils.MongoUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BaseRepository<T> implements IRepository<T> {

	protected Class clazz;

	protected MongoTemplate mongoTemplate;

	Logger logger = LoggerFactory.getLogger(BaseRepository.class);

	public BaseRepository(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public List<T> findAll() {
		return mongoTemplate.findAll(clazz);
	}

	@Override
	public Object findById(String id) {
		return mongoTemplate.findById(id, clazz);
	}

	@Override
	public <S extends T> S save(S object) {
		mongoTemplate.save(object);
		return object;
	}

	@Override
	public List<T> search(String column, Object value) {
		Query searchQuery = new Query();
		if (value instanceof String) {
			String transformedValue = (String) value;
			if (MongoUtils.checkIfHasSpecialCharacter(transformedValue))
				transformedValue = MongoUtils.escapeMetaCharacters(transformedValue);
			searchQuery.addCriteria(
					Criteria.where(column).regex(Pattern.compile(transformedValue, Pattern.CASE_INSENSITIVE)));
		} else {
			searchQuery.addCriteria(Criteria.where(column).is(value));
		}
		return mongoTemplate.find(searchQuery, clazz);
	}

	@Override
	public List<T> search(JsonObject object) {
		Query query = new Query();
		Set<Map.Entry<String, JsonElement>> entries = object.entrySet();
		for (Map.Entry<String, JsonElement> entry : entries) {
			if (entry.getValue() != null) {
				query.addCriteria(Criteria.where(entry.getKey())
						.regex(Pattern.compile(entry.getValue().getAsString(), Pattern.CASE_INSENSITIVE)));
				break;// TODO - fix me - should be possible to provide multiple options
				// TODO - should be refectored !!!!!
			}
		}
		List<T> results = mongoTemplate.find(query, clazz);
		return results;
	}

	@Override
	public void deleteById(Serializable objId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(T obj) {
		mongoTemplate.remove(obj);
	}

	@Override
	public void setRepoClass(Class clazz) {
		this.clazz = clazz;

	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	@Override
	public Object update(T object, String id) {
		Query searchQuery = new Query();
		searchQuery.addCriteria(Criteria.where("_id").is(id));
		if (mongoTemplate.exists(searchQuery, clazz)) {
			mongoTemplate.save(object);
			return this.findById(id);
		}
		return null;
	}

}
