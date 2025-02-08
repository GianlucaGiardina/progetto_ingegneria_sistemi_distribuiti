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
 * STRATEGY concreta per "Summarization":
 * gestisce la logica di invio/ricezione su RabbitMQ e salvataggio dati
 * specifici a Summarization.
 */
@Service
public class SummarizationStrategy implements ProcessingStrategy {

    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_USER = "user";
    private static final String RABBITMQ_PASS = "pass";
    private static final String QUEUE_SUMMARIZATION = "summarize_queue";

    @Autowired
    private RequestResultsRepository requestResultsRepository;

    @Autowired
    private RequestStateRepository requestStateRepository;

    @Override
    public void process(String requestId, String extractedText) throws Exception {
        // 1) Connessione a RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername(RABBITMQ_USER);
        factory.setPassword(RABBITMQ_PASS);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            final String correlationId = UUID.randomUUID().toString();
            final String replyQueue = "amq.rabbitmq.reply-to";

            final String[] response = new String[1];

            // Callback di ricezione
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    response[0] = new String(delivery.getBody(), StandardCharsets.UTF_8);

                    // 2) Salviamo la risposta su RequestResults
                    RequestResults results = requestResultsRepository.findById(requestId)
                            .orElseThrow(() -> new RuntimeException("RequestResults not found for id: " + requestId));
                    
                    results.setSummarizationResult(response[0]);
                    requestResultsRepository.save(results);

                    // 3) Aggiorniamo lo stato a "processed" (per Summarization)
                    requestStateRepository.findById(requestId).ifPresent(state -> {
                        state.setSummarizationStatus("processed");
                        requestStateRepository.save(state);
                    });
                }
            };

            // In ascolto di risposte sulla coda di reply
            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .messageId(requestId)
                    .replyTo(replyQueue)
                    .build();

            // 4) Invio effettivo del messaggio
            String messageToSend = String.format("{\"text\":\"%s\"}", extractedText);
            channel.basicPublish("", QUEUE_SUMMARIZATION, props, messageToSend.getBytes(StandardCharsets.UTF_8));

            // 5) Attesa risposta "bloccante" (semplice loop)
            while (response[0] == null) {
                Thread.sleep(100);
            }
        }
    }
}
