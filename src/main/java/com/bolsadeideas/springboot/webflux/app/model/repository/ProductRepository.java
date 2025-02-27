package com.bolsadeideas.springboot.webflux.app.model.repository;

import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    public Mono<Product> findByName(String name);
}
