package com.onefineday01.shortify.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onefineday01.shortify.entity.Url;
import com.onefineday01.shortify.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitConsumerService {

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    RabbitMQService rabbitMQService;

    @Autowired
    UrlService urlService;

    @RabbitListener(queues = "short_url")
    private void shortUrlMapping(String urlJson) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Required for LocalDateTime
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Optional: Ignore null values
            Url url = objectMapper.readValue(urlJson, Url.class);
            urlRepository.save(url);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "clicked")
    private void logClick(String shortCode) {
        try {
            boolean updatedRows = urlService.incrementClickCount(shortCode.trim());
            if (!updatedRows) {
//                throw new AmqpRejectAndDontRequeueException("URL not found, re-queuing message");
            }
        } catch (AmqpRejectAndDontRequeueException e) {
            log.warn("Re-queuing message for shortCode {}: {}", shortCode, e.getMessage());
            rabbitMQService.publish("clicked", shortCode);
        } catch (Exception e) {
            log.error("Error processing message for shortCode {}: {}", shortCode, e.getMessage());
        }
    }
}
