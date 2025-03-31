package com.onefineday01.shortify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefineday01.shortify.entity.Url;
import com.onefineday01.shortify.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UrlService {

    @Autowired
    ShortCodeGenerator shortCodeGenerator;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    RabbitMQService rabbit;

    @Autowired
    RedisTemplate<String, String> redis;

    public Url generateShortUrl(String longUrl, int expirySeconds) {
        String shortCode = shortCodeGenerator.generateShortCode();
        Url url = Url.builder().
                longUrl(longUrl).
                shortCode(shortCode)
                .expiresAt(LocalDateTime.now().plusSeconds(expirySeconds))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        redis.opsForValue().set(shortCode, longUrl);
        redis.expire(shortCode, 15, TimeUnit.MINUTES);
        rabbit.publish("short_url", url);

        return url;
    }

    public String getLongUrl(String shortCode) {
        String longUrl = redis.opsForValue().get(shortCode);
        if(longUrl ==  null) {
            Url url = urlRepository.findByShortCode(shortCode);
            if(url == null) {
                return null;
            }
            longUrl = url.getLongUrl();
            redis.opsForValue().set(shortCode, longUrl);
            redis.expire(shortCode, 15, TimeUnit.MINUTES);
        }
        rabbit.publish("clicked", shortCode);
        return longUrl;
    }

    @Transactional
    public boolean incrementClickCount(String shortCode) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            shortCode = objectMapper.readValue(shortCode, String.class);
//            shortCode = shortCode.replaceAll("^\"|\"$", "");  // Removes leading and trailing quotes
            int updatedRows = urlRepository.incrementClickCount(shortCode);
            return updatedRows > 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }
}
