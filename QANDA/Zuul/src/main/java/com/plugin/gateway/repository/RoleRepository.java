package com.plugin.gateway.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.plugin.gateway.auth.model.Role;

public interface RoleRepository extends MongoRepository<Role, String> {

	Role findByRole(String role);
}
