package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.rabbitmq.client.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class CentralServerController {

    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_USER = "user";
    private static final String RABBITMQ_PASS = "pass";
    private static final String QUEUE_NAME = "test_coda";

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/process")
    public ResponseEntity<?> handleRequest(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {

        String fileId = UUID.randomUUID().toString(); // Genera un fileId univoco
        String requestId = UUID.randomUUID().toString(); // Genera un requestId univoco

        // Rispondi subito al client con request_id
        executorService.submit(() -> processInBackground(file, userId, fileId, requestId));

        return ResponseEntity.ok().body("{\"requestId\": \"" + requestId + "\"}");
    }

    private void processInBackground(MultipartFile file, String userId, String fileId, String requestId) {
        try {
            // Estrarre il testo dal file
            String fileText = extractTextFromFile(file);

            // Invia il messaggio a RabbitMQ e resta in attesa della risposta
            String response = sendMessageToQueue(userId, fileId, requestId, fileText);

            // Aggiorna lo stato della richiesta con la risposta ricevuta
            updateStatus(requestId, response);

        } catch (Exception e) {
            updateStatus(requestId, "Errore durante l'elaborazione: " + e.getMessage());
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8); // Placeholder, sostituire con parser PDF
    }

    private String sendMessageToQueue(String userId, String fileId, String requestId, String message) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername(RABBITMQ_USER);
        factory.setPassword(RABBITMQ_PASS);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            final String correlationId = UUID.randomUUID().toString();
            final String replyQueue = "amq.rabbitmq.reply-to";

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueue)
                    .build();

            String messageToSend = String.format("{\"userId\": \"%s\", \"fileId\": \"%s\", \"requestId\": \"%s\", \"text\": \"%s\"}", 
                                                  userId, fileId, requestId, message);
            channel.basicPublish("", QUEUE_NAME, props, messageToSend.getBytes(StandardCharsets.UTF_8));

            final String[] response = new String[1];
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    response[0] = new String(delivery.getBody(), StandardCharsets.UTF_8);
                }
            };
            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});

            // Attende la risposta
            while (response[0] == null) {
                Thread.sleep(100);
            }
            return response[0];
        }
    }

    private void updateStatus(String requestId, String response) {
        System.out.println("Aggiornamento stato - RequestID: " + requestId + " - Stato: " + response);
        // Simula un aggiornamento in un database
    }
}
