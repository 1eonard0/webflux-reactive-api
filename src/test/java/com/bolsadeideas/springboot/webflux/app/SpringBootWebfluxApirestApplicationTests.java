package com.bolsadeideas.springboot.webflux.app;

import com.bolsadeideas.springboot.webflux.app.model.documents.Category;
import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import com.bolsadeideas.springboot.webflux.app.model.service.ICategoryService;
import com.bolsadeideas.springboot.webflux.app.model.service.IProductService;
import com.bolsadeideas.springboot.webflux.app.model.service.impl.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private IProductService productService;

	@Autowired
	private ICategoryService categoryService;

	@Value("${base.product.path}")
	private String productBaseURL;

	@Test
	void listAll() {
		webTestClient.get()
				.uri(URI.create(productBaseURL))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				.consumeWith(response -> {
					List<Product> productList = response.getResponseBody();
                    Assertions.assertFalse(productList.isEmpty());
				});
	}

	@Test
	void detail() {

		Product prod = productService.findByName("Samsung A70").block();

		webTestClient.get()
				.uri(productBaseURL.concat("/{id}"), Collections.singletonMap("id", prod.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product = response.getResponseBody();
                    Assertions.assertFalse(product.getId().isEmpty());
					Assertions.assertEquals("Samsung A70", product.getName());
				});
				/*.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Samsung A70");*/
	}

	@Test
	void create(){
		Category category = categoryService.findByName("Electronic").block();
		Product prod = new Product("Samsung S25", 3200500.00, category);

		webTestClient.post()
				.uri(URI.create(productBaseURL))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(prod), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Samsung S25")
				.jsonPath("$.category.name").isEqualTo("Electronic");
	}

	@Test
	void createWithConsumeWith(){
		Category category = categoryService.findByName("Electronic").block();
		Product prod = new Product("Samsung S25", 3200500.00, category);

		webTestClient.post()
				.uri(URI.create(productBaseURL))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(prod), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product = response.getResponseBody();

					Assertions.assertFalse(product.getId().isEmpty());
					Assertions.assertEquals("Samsung S25", product.getName());
					Assertions.assertEquals("Electronic", product.getCategory().getName());
				});
	}

	@Test
	void edit(){
		Category category = categoryService.findByName("Electronic").block();
		Product prod = productService.findByName("Samsung A70").block();

		Product updatedProd = new Product(prod.getName().concat(" UPDATED"), 240.00, category);

		webTestClient.put()
				.uri(productBaseURL.concat("/{id}"), Collections.singletonMap("id", prod.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(updatedProd), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo(prod.getName().concat(" UPDATED"));

	}

	@Test
	void delete(){
		Product product = productService.findByName("Play Station 5 (PS5)").block();
		webTestClient.delete()
				.uri(productBaseURL.concat("/{id}"), Collections.singletonMap("id", product.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody().isEmpty();

		webTestClient.get()
				.uri(productBaseURL.concat("/{id}"), Collections.singletonMap("id", product.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody().isEmpty();
	}

}
