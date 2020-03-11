package com.plugin.qanda.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.plugin.qanda.entity.model.Answer;
@Repository
public interface AnswerRepository extends MongoRepository<Answer, String> {

}
