package com.bolsadeideas.springboot.webflux.app.model.service.impl;

import com.bolsadeideas.springboot.webflux.app.model.documents.Category;
import com.bolsadeideas.springboot.webflux.app.model.repository.CategoryRepository;
import com.bolsadeideas.springboot.webflux.app.model.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Flux<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Mono<Category> findById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Mono<Category> save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Mono<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

}
