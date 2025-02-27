package com.bolsadeideas.springboot.webflux.app.model.service.impl;

import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import com.bolsadeideas.springboot.webflux.app.model.repository.ProductRepository;
import com.bolsadeideas.springboot.webflux.app.model.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Flux<Product> findAllWthNameInUpperCase() {
        return productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .log();
    }

    @Override
    public Flux<Product> findAllWthDelay() {
        return productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .delayElements(Duration.ofSeconds(1))
                .log();
    }

    @Override
    public Flux<Product> findAllRepeat() {
        return productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                }).repeat(5000)
                .log();
    }

    @Override
    public Mono<Product> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public Mono<Product> save(Product product) {
        if(product.getCreatedAt() == null){
            product.setCreatedAt(new Date());
        }
        return productRepository.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return productRepository.delete(product);
    }

    @Override
    public Mono<Product> findByName(String name) {
        return productRepository.findByName(name);
    }
}
