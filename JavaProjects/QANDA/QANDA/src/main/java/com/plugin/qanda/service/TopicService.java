/**
 * 
 */
package com.plugin.qanda.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.plugin.qanda.base.exception.BaseRuntimeException;
import com.plugin.qanda.base.service.BaseService;
import com.plugin.qanda.base.utils.HttpUtils;
import com.plugin.qanda.base.utils.MongoUtils;
import com.plugin.qanda.entity.model.Answer;
import com.plugin.qanda.entity.model.Post;
import com.plugin.qanda.entity.model.Topic;
import com.plugin.qanda.repository.PostRepository;
import com.plugin.qanda.repository.TopicRepository;
import com.plugins.qanda.base.enums.HttpHeaders;

/**
 * @author Sankha
 *
 */
@Service
public class TopicService extends BaseService<Topic> {
	Logger logger = LoggerFactory.getLogger(TopicService.class);
	
	TopicRepository topicRepository;
	@Autowired
	AnswerService answerService;
	public TopicService() {
	}
	@Autowired
	public TopicService(TopicRepository topicRepository,MongoTemplate mongoTemplate) {
		super(topicRepository,mongoTemplate);
		this.topicRepository = topicRepository;
	}
	
}
