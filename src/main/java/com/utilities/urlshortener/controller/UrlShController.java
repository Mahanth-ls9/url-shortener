package com.utilities.urlshortener.controller;

import com.mongodb.DuplicateKeyException;
import com.utilities.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class UrlShController {

    private final UrlService urlService;
    public UrlShController(UrlService urlService) {this.urlService = urlService;}

    private static final Logger logger = LoggerFactory.getLogger(UrlShController.class);


    @Operation(summary = "Create short URL", description = "Converts a long URL to short URL")
    @ApiResponse(responseCode = "200", description = "Short URL created successfully")
    @PostMapping("/shorten")
    public Mono<ResponseEntity<String>> shortenUrl(@RequestParam String originalUrl) {
        logger.info("Received request to shorten URL: {}", originalUrl);
        return urlService.createShortUrl(originalUrl)
                .doOnNext(shortUrl -> logger.debug("Generated short URL: {}", shortUrl))
                .map(ResponseEntity::ok)
                .doOnError(e -> logger.error("Error shortening URL", e))
                .onErrorResume(DuplicateKeyException.class,
                        e -> {
                            logger.warn("Duplicate key detected for URL: {}", originalUrl);
                            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()));
                        });
    }

    @Operation(summary = "Redirect to original URL", description = "Finds original URL from short key and redirects")
    @ApiResponse(responseCode = "302", description = "Redirects to original URL")
    @ApiResponse(responseCode = "404", description = "Short URL not found")
    @GetMapping("/{shortKey}")
    public Mono<ResponseEntity<Object>> redirectToOriginalUrl(@PathVariable String shortKey) {
        logger.info("Received redirect request for short key: {}", shortKey);
        return urlService.getOriginalUrl(shortKey)
                .doOnNext(url -> logger.debug("Found original URL: {}", url))
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", url)
                        .build())
                .doOnError(e -> logger.error("Error redirecting", e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get all short keys",
            description = "Returns a list of all existing short URLs",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    schema = @Schema(implementation = String.class, type = "array"),
                                    examples = @ExampleObject(
                                            name = "sample",
                                            value = "[\"ZWVmNWZk\", \"OGI2NzQ3\"]"
                                    )
                            )
                    )
            }
    )
    public Flux<String> getAllShortKeys() {
        return urlService.getAllShortKeys()
                .doOnNext(key -> {
                    if (key.length() != 8) {
                        logger.warn("Found malformed key in response: {}", key);
                    }
                })
                .doOnSubscribe(sub -> logger.info("Client requested all short keys"))
                .doOnComplete(() -> logger.debug("Completed streaming all keys"));
    }

}
