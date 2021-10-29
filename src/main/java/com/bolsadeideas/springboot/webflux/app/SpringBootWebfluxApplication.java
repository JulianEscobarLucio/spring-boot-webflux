package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.bolsadeideas.springboot.webflux.app.dao.ProductoDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.services.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private ProductoService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computacion");
		Categoria muebles = new Categoria("Muebles");
		
    	Flux.just(electronico, deporte, computacion, muebles)
		.flatMap(service::saveCategoria)
		.doOnNext(c -> {
			log.info("Categoria creada" + c.getNombre() + ",  Id: " + c.getId());
		}).thenMany(Flux.just(new Producto("TV Panasonic Pantall LCD", 456.89, electronico),
						new Producto("Computación", 177.89, electronico),
						new Producto("Apple ipod", 46.89, electronico), new Producto("Mesa", 46.89, muebles))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				})).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));

		Flux.just(new Producto("TV Panasonic Pantall LCD", 456.89, electronico),
				new Producto("Computación", 177.89, electronico), new Producto("Apple ipod", 46.89, electronico),
				new Producto("Mesa", 46.89, muebles)).flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				}).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}

}
