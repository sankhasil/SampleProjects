package com.sdgt.gateway.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sdgt.gateway.auth.model.User;

public interface UserRepository extends MongoRepository<User, String> {

	User findByEmail(String email);
}
