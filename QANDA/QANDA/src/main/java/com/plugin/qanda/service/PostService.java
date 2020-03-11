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
import com.plugin.qanda.repository.AnswerRepository;
import com.plugin.qanda.repository.PostRepository;
import com.plugins.qanda.base.enums.HttpHeaders;

/**
 * @author Sankha
 *
 */
@Service
public class PostService extends BaseService<Post> {
	Logger logger = LoggerFactory.getLogger(PostService.class);

	PostRepository postRepository;
	@Autowired
	AnswerService answerService;

	public PostService() {
	}

	@Autowired
	public PostService(PostRepository postRepository, MongoTemplate mongoTemplate) {
		super(postRepository, mongoTemplate);
		this.postRepository = postRepository;
	}

	@Override
	public Post create(Post object) {
		if (StringUtils.isNotBlank(object.getTopicId()))
			return super.create(object);
		throw new BaseRuntimeException("Post should have a Topic Id");

	}

	public List<Post> findAllQuestions() {
		List<Post> postList = new ArrayList<>();
		postRepository.findAllQuestionsByUser(HttpUtils.getHeader(HttpHeaders.USER_NAME)).forEach(postList::add);
		return postList;
	}

	public Post addAnswer(String postId, String answerContent) {
		if (StringUtils.isNoneBlank(postId, answerContent)) {
			Post questionToAnswer = findById(postId);
			if (questionToAnswer != null && questionToAnswer.isAQuestion()) {
				Answer answer = new Answer(answerContent);
				answer.setQuestionId(postId);
				questionToAnswer.addAnswer(answerService.create(answer));
				return update(questionToAnswer, postId);
			}
		}
		return null;
	}

	public List<Post> getPostsByTag(String tag) {
		List<Post> postList = new ArrayList<>();
		if (MongoUtils.checkIfHasSpecialCharacter(tag)) {
			tag = MongoUtils.escapeMetaCharacters(tag);
		}
		// TODO: sort posts by date modified
		postRepository.findByTag(tag).forEach(postList::add);
		return postList;
	}

	public List<Post> getPostsByText(String text) {
		List<Post> postList = new ArrayList<>();
		if (MongoUtils.checkIfHasSpecialCharacter(text)) {
			text = MongoUtils.escapeMetaCharacters(text);
		}
		// TODO: sort posts by date modified
		postRepository.findByText(text).forEach(postList::add);
		return postList;
	}
}
