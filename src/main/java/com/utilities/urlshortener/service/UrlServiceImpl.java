package com.utilities.urlshortener.service;

import com.utilities.urlshortener.model.UrlP;
import com.utilities.urlshortener.repository.MongoUrlRpo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;


@Service
public class UrlServiceImpl implements UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlServiceImpl.class);

        private final MongoUrlRpo urlRepository;

        @Autowired
        public UrlServiceImpl(MongoUrlRpo urlRepository) {
            this.urlRepository = urlRepository;
        }

        @Override
        public Mono<String> createShortUrl(String originalUrl) {
            String shortKey = generateShortKey();
            UrlP url = new UrlP();
            url.setOriginalUrl(originalUrl);
            url.setShortKey(shortKey);

            return urlRepository.save(url)
                    .map(savedUrl -> buildShortUrl(savedUrl.getShortKey()));
        }

        @Override
        public Mono<String> getOriginalUrl(String shortKey) {
            return urlRepository.findByShortKey(shortKey)
                    .map(UrlP::getOriginalUrl);
        }

    @Override
    public Flux<String> getAllShortKeys() {
        return urlRepository.findAllShortKeys()
                .map(UrlP::getShortKey)
                .doOnSubscribe(sub -> logger.debug("Fetching all short keys"))
                .doOnNext(key -> logger.trace("Found key: {}", key))
                .doOnComplete(() -> logger.debug("Completed fetching all keys"))
                .doOnError(e -> logger.error("Error fetching keys", e));
    }

    private String generateShortKey() {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(UUID.randomUUID().toString().getBytes())
                    .substring(0, 8);
        }

        private String buildShortUrl(String shortKey) {
            return "http://localhost:8080/api/url/" + shortKey;
        }
}
