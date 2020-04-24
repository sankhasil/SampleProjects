package com.sdgt.gateway.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sdgt.gateway.auth.model.Role;

public interface RoleRepository extends MongoRepository<Role, String> {

	Role findByRole(String role);
}
