/**
 * 
 */
package com.plugin.qanda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plugin.qanda.base.controller.BaseController;
import com.plugin.qanda.entity.model.Topic;
import com.plugin.qanda.service.TopicService;

/**
 * @author Sankha
 *
 */
@RestController
@CrossOrigin
@RequestMapping(value = {"/topic","/Topic"})
public class TopicController extends BaseController<Topic> {

	TopicService topicService;
	@Autowired
	public TopicController(TopicService topicService) {
		super(topicService);

	}
	
	
}
