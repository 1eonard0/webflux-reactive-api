package com.bolsadeideas.springboot.webflux.app.model.repository;

import com.bolsadeideas.springboot.webflux.app.model.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
    Mono<Category> findByName(String name);
}
