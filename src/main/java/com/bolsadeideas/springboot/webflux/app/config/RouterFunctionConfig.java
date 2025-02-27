package com.bolsadeideas.springboot.webflux.app.config;

import com.bolsadeideas.springboot.webflux.app.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    private static final String GET_PUT_DEL = "/api/v2/products/{id}";

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return route(GET("/api/v2/products"), handler::listAll)
                .andRoute(GET(GET_PUT_DEL), handler::detail)
                .andRoute(POST("/api/v2/products"), handler::create)
                .andRoute(PUT(GET_PUT_DEL), handler::update)
                .andRoute(DELETE(GET_PUT_DEL), handler::delete)
                .andRoute(POST("/api/v2/products/upload/{id}"), handler::uploadImage)
                .andRoute(POST("/api/v2/products/create-with-img"), handler::createWithImage);
    }
}
