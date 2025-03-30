package com.onefineday01.shortify.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQService {

    private final RabbitAdmin rabbitAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public RabbitMQService(ConnectionFactory connectionFactory) {
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    // Create Exchange
    public void createExchange(String exchangeName) {
        DirectExchange exchange = new DirectExchange(exchangeName, true, false);
        rabbitAdmin.declareExchange(exchange);
        System.out.println("Exchange created: " + exchangeName);
    }

    // Create Queue
    public void createQueue(String queueName) {
        Queue queue = new Queue(queueName, true);
        rabbitAdmin.declareQueue(queue);
        System.out.println("Queue created: " + queueName);
    }

    // Bind Queue to Exchange
    public void bindQueueToExchange(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder.bind(new Queue(queueName, true))
                .to(new DirectExchange(exchangeName, true, false))
                .with(routingKey);
        rabbitAdmin.declareBinding(binding);
        System.out.println("Queue " + queueName + " bound to exchange " + exchangeName + " with routing key: " + routingKey);
    }

    public void publish(String queueName, Object obj) {
        try{
            this.createQueue(queueName);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Required for LocalDateTime
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Optional: Ignore null values
            String jsonObj = objectMapper.writeValueAsString(obj);
            rabbitTemplate.convertAndSend("", queueName, jsonObj);
        } catch (Exception e) {
            log.error("Rabbit Publish Failed");
        }
    }
}