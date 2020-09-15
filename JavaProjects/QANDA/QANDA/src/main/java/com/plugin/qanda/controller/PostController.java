/**
 * 
 */
package com.plugin.qanda.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plugin.qanda.base.controller.BaseController;
import com.plugin.qanda.entity.model.Post;
import com.plugin.qanda.service.PostService;

/**
 * @author Sankha
 *
 */
@RestController
@CrossOrigin
@RequestMapping(value = {"/post","/Post"})
public class PostController extends BaseController<Post> {

	@Autowired
	PostService postService;
	@Autowired
	public PostController(PostService postService) {
		super(postService);

	}
	@GetMapping("/getByTag")
	public List<Post> getByTag(@RequestParam("value") String tag){
		return postService.getPostsByTag(tag);
	}
	
	
	@PostMapping("/answer/{postId}")
	public Post answerQuest(@PathVariable("postId")String postId,@RequestBody String answerContent){
		return postService.addAnswer(postId, answerContent);
	}
}
