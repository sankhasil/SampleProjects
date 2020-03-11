package com.plugin.qanda.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.plugin.qanda.entity.model.Post;
import com.plugin.qanda.repository.PostRepository;
import com.plugin.qanda.service.PostService;

@SpringBootTest
public class PostServiceTest {

	private static final String FIRSTQUESTION = "#firstquestion";
	private static final String TEST_POST = "test post?";
	@Mock
	private PostRepository postRepository;
	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private PostService postService = new PostService(postRepository, mongoTemplate);


	@BeforeEach
	public void setUp() {
		List<Post> aPostList = new ArrayList<Post>();
		Post aPost = new Post(TEST_POST,true);
		aPost.setTagList(Arrays.asList(FIRSTQUESTION,"#testTag"));
		aPostList.add(aPost);
		//FIXME: Mock not working Fails the test due to that
		when(postRepository.findByText(TEST_POST)).thenReturn(aPostList);
		when(postRepository.findByTag(FIRSTQUESTION)).thenReturn(aPostList);
	}
	@DisplayName("Simple Test to find post by text")
	@Test
	public void whenValidText_thenPostShouldBeFound() {
	    String testText = TEST_POST;
	    Post found = postService.getPostsByText(testText).get(0);
	    assertEquals(found.getText(), testText);
	     
	 }
	
	@DisplayName("Simple Test to find post by tag")
	@Test
	public void whenValidTag_thenPostShouldBeFound() {
	    String testTag = FIRSTQUESTION;
	    Post found = postService.getPostsByText(testTag).get(0);
	    assertTrue(found.getTagList().contains(testTag));
	     
	 }
}
