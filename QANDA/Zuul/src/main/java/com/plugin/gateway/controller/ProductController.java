package com.sdgt.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sdgt.gateway.auth.model.Product;
import com.sdgt.gateway.repository.ProductRepository;

import java.util.Optional;

@RestController
public class ProductController {

	@Autowired
    ProductRepository productRepository;

    @RequestMapping(method= RequestMethod.GET, value="/api/products")
    public Iterable<Product> product() {
        return productRepository.findAll();
    }

    @RequestMapping(method= RequestMethod.POST, value="/api/products")
    public String save(@RequestBody Product product) {
        productRepository.save(product);

        return product.getId();
    }

    @RequestMapping(method= RequestMethod.GET, value="/api/products/{id}")
    public Optional<Product> show(@PathVariable String id) {
        return productRepository.findById(id);
    }

    @RequestMapping(method= RequestMethod.PUT, value="/api/products/{id}")
    public Product update(@PathVariable String id, @RequestBody Product product) {
    	Optional<Product> prod = productRepository.findById(id);
        if(product.getProdName() != null)
            prod.get().setProdName(product.getProdName());
        if(product.getProdDesc() != null)
            prod.get().setProdDesc(product.getProdDesc());
        if(product.getProdPrice() != null)
            prod.get().setProdPrice(product.getProdPrice());
        if(product.getProdImage() != null)
            prod.get().setProdImage(product.getProdImage());
        productRepository.save(prod.get());
        return prod.get();
    }

    @RequestMapping(method= RequestMethod.DELETE, value="/api/products/{id}")
    public String delete(@PathVariable String id) {
        Optional<Product> product = productRepository.findById(id);
        productRepository.delete(product.get());

        return "product deleted";
    }
}
