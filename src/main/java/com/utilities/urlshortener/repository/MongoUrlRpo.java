package com.utilities.urlshortener.repository;



import com.utilities.urlshortener.model.UrlP;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MongoUrlRpo extends ReactiveMongoRepository<UrlP, String> {

    Mono<UrlP> findByShortKey(String shortKey);
    Mono<Boolean> existsByShortKey(String shortKey);

    // Add this new method
    @Query(value = "{}", fields = "{ 'shortKey' : 1 }")
    Flux<UrlP> findAllShortKeys();
}