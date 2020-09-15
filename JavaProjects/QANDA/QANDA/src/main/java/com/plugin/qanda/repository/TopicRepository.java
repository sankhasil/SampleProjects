package com.plugin.qanda.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.plugin.qanda.entity.model.Topic;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {

}
