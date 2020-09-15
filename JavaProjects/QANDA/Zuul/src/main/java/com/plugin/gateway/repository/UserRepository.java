package com.plugin.gateway.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.plugin.gateway.auth.model.User;

public interface UserRepository extends MongoRepository<User, String> {

	User findByEmail(String email);
}
