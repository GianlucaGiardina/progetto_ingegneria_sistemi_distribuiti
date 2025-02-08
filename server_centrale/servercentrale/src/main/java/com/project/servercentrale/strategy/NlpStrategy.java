package com.project.servercentrale.strategy;

import com.project.servercentrale.models.RequestResults;
import com.project.servercentrale.repositories.RequestResultsRepository;
import com.project.servercentrale.repositories.RequestStateRepository;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * STRATEGY concreta per "NLP":
 * gestisce la logica di invio/ricezione su RabbitMQ e salvataggio dati
 * specifici a NLP.
 */
@Service
public class NlpStrategy implements ProcessingStrategy {

    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_USER = "user";
    private static final String RABBITMQ_PASS = "pass";
    private static final String QUEUE_NLP = "nlp_queue";

    @Autowired
    private RequestResultsRepository requestResultsRepository;

    @Autowired
    private RequestStateRepository requestStateRepository;

    @Override
    public void process(String requestId, String extractedText) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername(RABBITMQ_USER);
        factory.setPassword(RABBITMQ_PASS);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            final String correlationId = UUID.randomUUID().toString();
            final String replyQueue = "amq.rabbitmq.reply-to";

            final String[] response = new String[1];

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    response[0] = new String(delivery.getBody(), StandardCharsets.UTF_8);

                    // Salviamo risultato su RequestResults
                    RequestResults results = requestResultsRepository.findById(requestId)
                            .orElseThrow(() -> new RuntimeException("RequestResults not found for id: " + requestId));
                    
                    results.setNlpResult(response[0]);
                    requestResultsRepository.save(results);

                    // Aggiorniamo stato a "processed" (per NLP)
                    requestStateRepository.findById(requestId).ifPresent(state -> {
                        state.setNlpStatus("processed");
                        requestStateRepository.save(state);
                    });
                }
            };

            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .messageId(requestId)
                    .replyTo(replyQueue)
                    .build();

            String messageToSend = String.format("{\"text\":\"%s\"}", extractedText);
            channel.basicPublish("", QUEUE_NLP, props, messageToSend.getBytes(StandardCharsets.UTF_8));

            // Attesa risposta
            while (response[0] == null) {
                Thread.sleep(100);
            }
        }
    }
}
