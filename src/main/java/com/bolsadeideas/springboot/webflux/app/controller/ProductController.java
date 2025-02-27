package com.bolsadeideas.springboot.webflux.app.controller;

import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import com.bolsadeideas.springboot.webflux.app.model.service.impl.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Value("${config.upload.path}")
    private String path;

    @PostMapping("/create-with-image")
    public Mono<ResponseEntity<Product>> createWithImg(Product product, @RequestPart FilePart file){

        product.setImage(UUID.randomUUID().toString().concat("-").concat(file.filename())
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));

        return file.transferTo(new File(path.concat(product.getImage()))).then(productService.save(product))
                .map(prod -> ResponseEntity
                        .created(URI.create("/v1/product/".concat(product.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(prod));
    }

    @PostMapping("/upload/image/{id}")
    public Mono<ResponseEntity<Product>> uploadImg(@PathVariable String id, @RequestPart FilePart file){
        return productService.findById(id).flatMap(prod -> {
            prod.setImage(UUID.randomUUID().toString().concat("-").concat(file.filename())
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("\\", ""));

            return file.transferTo(new File(path.concat(prod.getImage()))).then(productService.save(prod));
        }).map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> listAll(){
        return Mono.just(ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll()));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> view(@PathVariable String id){
        return productService.findById(id).map(prod ->
            ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prod)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Product>> create(@RequestBody Product product){
        return productService.save(product)
                .map(prod -> ResponseEntity
                        .created(URI.create("/v1/product/".concat(prod.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(prod));
    }

    @PostMapping("/create-validated")
    public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<Product> monoProduct){
        Map<String, Object> response = new HashMap<>();

        return monoProduct.flatMap( product -> {
            response.put("product", product);
            return productService.save(product)
                    .map(prod -> ResponseEntity
                            .created(URI.create("/v1/product/".concat(prod.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(response));
        }).onErrorResume(t -> Mono.just(t).cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors()))
                .flatMapMany(Flux::fromIterable)
                .map(fieldError -> "Field ".concat(fieldError.getField()).concat(" ").concat(fieldError.getDefaultMessage()))
                .collectList()
                .flatMap(list -> {
                    response.put("errors", list);
                    return Mono.just(ResponseEntity.badRequest().body(response));
                }));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> edit(@RequestBody Product product, @PathVariable String id) {
        return productService.findById(id)
                .flatMap(prod -> {
                        prod.setName(product.getName());
                        prod.setPrice(product.getPrice());
                        return productService.save(prod);
                }).map(prod -> ResponseEntity.created(URI.create("/v1/product/".concat(prod.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(prod))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id){
        return productService.findById(id).flatMap(prod -> {
            return productService.delete(prod).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
