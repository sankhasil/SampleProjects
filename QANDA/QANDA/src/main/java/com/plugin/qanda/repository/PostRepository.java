package com.plugin.qanda.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.plugin.qanda.entity.model.Post;
@Repository
public interface PostRepository extends MongoRepository<Post, String> {

	@Query("{'tagList' : {$regex : ?0}}")
	List<Post> findByTag(String tag);

	@Query("{'text' : {$regex : ?0}}")
	List<Post> findByText(String text);

	@Query("{'createdBy' : ?0, 'isQuestion':true}")
	List<Post> findAllQuestionsByUser(String user);
}
