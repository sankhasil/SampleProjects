/**
 * 
 */
package com.plugin.qanda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.plugin.qanda.base.service.BaseService;
import com.plugin.qanda.entity.model.Answer;
import com.plugin.qanda.repository.AnswerRepository;

/**
 * @author Sankha
 *
 */
@Service
public class AnswerService extends BaseService<Answer> {

	AnswerRepository answerRepository;

	@Autowired
	public AnswerService(AnswerRepository answerRepository, MongoTemplate mongoTemplate) {
		super(answerRepository, mongoTemplate);
		this.answerRepository = answerRepository;
	}
}
