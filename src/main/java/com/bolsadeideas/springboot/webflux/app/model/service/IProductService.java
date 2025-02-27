package com.bolsadeideas.springboot.webflux.app.model.service;

import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductService {

    public Flux<Product> findAll();

    public Flux<Product> findAllWthNameInUpperCase();

    public Flux<Product> findAllWthDelay();

    public Flux<Product> findAllRepeat();

    public Mono<Product> findById(String id);

    public Mono<Product> save(Product product);

    public Mono<Void> delete(Product product);

    public Mono<Product> findByName(String name);

}
