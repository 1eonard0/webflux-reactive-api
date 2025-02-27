package com.bolsadeideas.springboot.webflux.app;

import com.bolsadeideas.springboot.webflux.app.model.documents.Category;
import com.bolsadeideas.springboot.webflux.app.model.documents.Product;
import com.bolsadeideas.springboot.webflux.app.model.repository.CategoryRepository;
import com.bolsadeideas.springboot.webflux.app.model.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {


	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reactiveMongoTemplate.dropCollection("products").subscribe();
		reactiveMongoTemplate.dropCollection("categories").subscribe();

		Category electronic = new Category("Electronic");
		Category gaming = new Category("Gaming");

		Flux.just(electronic, gaming)
				.flatMap(c -> categoryRepository.save(c))
				.thenMany(
						Flux.just(new Product("Samsung A70", 63000.00, electronic),
										new Product("Samsung A63", 250000.00, electronic),
										new Product("Samsung A24", 200000.00, electronic),
										new Product("Samsung S23", 2000000.00, electronic),
										new Product("Samsung s23 plus", 2300000.00, electronic),
										new Product("Samsung s24", 2800000.00, electronic),
										new Product("Samsung s24 plus", 360000.00, electronic),
										new Product("Play Station 5 (PS5)", 1340000.00, gaming))
								.flatMap(product -> {
									product.setCreatedAt(new Date());
									return productRepository.save(product);
								})
				)
				.subscribe(product -> log.info(product.getId()));
	}
}
