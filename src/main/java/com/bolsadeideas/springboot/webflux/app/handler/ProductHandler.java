package com.bolsadeideas.springboot.webflux.app.handler;

import com.bolsadeideas.springboot.webflux.app.model.documents.Category;
import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import com.bolsadeideas.springboot.webflux.app.model.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {

    @Autowired
    private ProductService productService;

    @Value("${config.upload.path}")
    private String path;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> createWithImage(ServerRequest req){
        Mono<Product> monoProd = req.multipartData().map(mt -> {
            FormFieldPart name = (FormFieldPart) mt.toSingleValueMap().get("name");
            FormFieldPart price = (FormFieldPart) mt.toSingleValueMap().get("price");
            FormFieldPart categoryId = (FormFieldPart) mt.toSingleValueMap().get("category.id");
            FormFieldPart categoryName = (FormFieldPart) mt.toSingleValueMap().get("category.name");

            Category cat = new Category(categoryName.value());
            cat.setId(categoryId.value());

            return new Product(name.value(), Double.valueOf(price.value()), cat);
        });

        return req.multipartData().map( multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file ->
                        monoProd.flatMap( product -> {
                            product.setImage(UUID.randomUUID().toString().concat("-").concat(file.filename())
                                    .replace(" ","")
                                    .replace(":","")
                                    .replace("\\",""));
                            return file.transferTo(new File(path.concat(product.getImage()))).then(productService.save(product));
                        })).flatMap(product -> ServerResponse.created(URI.create("/api/v2/products".concat(product.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(product)));
    }


    public Mono<ServerResponse> uploadImage(ServerRequest req){
        String id = req.pathVariable("id");
        return req.multipartData().map( multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file ->
                    productService.findById(id).flatMap( product -> {
                        product.setImage(UUID.randomUUID().toString().concat("-").concat(file.filename())
                                .replace(" ","")
                                .replace(":","")
                                .replace("\\",""));
                        return file.transferTo(new File(path.concat(product.getImage()))).then(productService.save(product));
                    })).flatMap(product -> ServerResponse.created(URI.create("/api/v2/products".concat(product.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(fromValue(product)))
                            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listAll(ServerRequest req){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> detail(ServerRequest req) {
        String id = req.pathVariable("id");
        return productService.findById(id)
                .flatMap(prod -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(prod)))
                        .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        Mono<Product> product = req.bodyToMono(Product.class);

        return product.flatMap(product1 -> {
                    Errors errors = new BeanPropertyBindingResult(product1, Product.class.getName());
                    validator.validate(product1, errors);

                    if(errors.hasErrors()){
                        return Flux.fromIterable(errors.getFieldErrors())
                                .map(fieldError -> "The Field ".concat(fieldError.getField()).concat(" ").concat(fieldError.getDefaultMessage()))
                                .collectList()
                                .flatMap(list -> ServerResponse.badRequest().body(fromValue(list)));
                    }else {
                        return productService.save(product1).flatMap(prod -> ServerResponse.created(URI.create("/api/v2/products".concat(prod.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(prod)));
                    }
                });

    }

    public Mono<ServerResponse> update(ServerRequest req){
        String id = req.pathVariable("id");
        Mono<Product> product = req.bodyToMono(Product.class);
        Mono<Product> productDB = productService.findById(id);

        return productDB.zipWith(product, (db, reqProd) -> {
            db.setName(reqProd.getName());
            db.setPrice(reqProd.getPrice());
            return db;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.save(p), Product.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        String id = req.pathVariable("id");
        Mono<Product> productDB = productService.findById(id);

        return productDB.flatMap( prod -> productService.delete(prod).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
