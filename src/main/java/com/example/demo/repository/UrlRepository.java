package com.example.demo.repository;

import com.example.demo.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Extending JpaRepository<Url, Long> gives you save(), findById(), findAll(), deleteById(), etc.
// for free. Spring generates a working implementation of this interface at runtime --
// you will never write an "implements UrlRepository" class yourself.
public interface UrlRepository extends JpaRepository<Url, Long> {

    // Spring Data JPA parses this METHOD NAME and builds:
    // SELECT * FROM urls WHERE short_code = ?
    // The signature is the query -- no SQL, no implementation.
    Optional<Url> findByShortCode(String shortCode);
}