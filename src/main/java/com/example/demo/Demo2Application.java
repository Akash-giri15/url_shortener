package com.example.demo;

import com.example.demo.model.Url;
import com.example.demo.repository.UrlRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class Demo2Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo2Application.class, args);
    }

    // TEMPORARY -- delete this bean once you've verified the row in Postgres.
    // Spring calls run() exactly once, right after the whole application context loads --
    // and it injects UrlRepository via the method parameter, the same constructor-style
    // DI idea from Checkpoint 1, just applied to a @Bean method instead of a class constructor.
//    @Bean
//    CommandLineRunner testInsert(UrlRepository urlRepository) {
//        return args -> {
//            Url saved = urlRepository.save(new Url("test123", "https://www.anthropic.com"));
//            System.out.println("Saved URL with generated ID: " + saved.getId());
//        };
//    }
}
