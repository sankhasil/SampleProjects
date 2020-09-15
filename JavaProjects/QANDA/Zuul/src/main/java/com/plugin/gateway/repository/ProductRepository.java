package com.plugin.gateway.repository;

import org.springframework.data.repository.CrudRepository;

import com.plugin.gateway.auth.model.Product;

public interface ProductRepository extends CrudRepository<Product, String> {
	
	@Override
    void delete(Product deleted);
}
