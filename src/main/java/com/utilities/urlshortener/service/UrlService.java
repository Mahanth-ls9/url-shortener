package com.utilities.urlshortener.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UrlService {
    /**
     * Creates a shortened URL for the given original URL
     * @param originalUrl The original long URL to shorten
     * @return A Mono containing the shortened URL
     */
    Mono<String> createShortUrl(String originalUrl);

    /**
     * Retrieves the original URL for the given short key
     * @param shortKey The short key to lookup
     * @return A Mono containing the original URL if found
     */
    Mono<String> getOriginalUrl(String shortKey);

    /**
     * Retrieves teh full list of Short Keys generated
     * @return
     */
    Flux<String> getAllShortKeys();
}
