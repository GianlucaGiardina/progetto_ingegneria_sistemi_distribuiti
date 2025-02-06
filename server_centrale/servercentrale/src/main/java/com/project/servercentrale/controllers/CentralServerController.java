package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rabbitmq.client.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
@RestController
@RequestMapping("/api")
public class CentralServerController {

    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_USER = "user";
    private static final String RABBITMQ_PASS = "pass";
    private static final String QUEUE_SUMMARIZATION = "test_queue";
    private static final String QUEUE_NLP = "nlp_queue";
    private static final String EXTRACT_TEXT_API = "http://localhost:5001/apii/extract/text";

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/process")
    public ResponseEntity<?> handleRequest(@RequestBody RequestPayload payload) {
        String requestId = UUID.randomUUID().toString();

        // Simulazione salvataggio stato su database
        // saveStatusToDatabase(requestId, payload.getUserId(), payload.getFileId(), payload.getServices());

        executorService.submit(() -> processInBackground(payload, requestId));
        return ResponseEntity.ok().body("{" + "\"requestId\": \"" + requestId + "\"}" );
    }

    private void processInBackground(RequestPayload payload, String requestId) {
        try {
            String extractedText = extractTextFromFile(payload.getFile());
            for (String service : payload.getServices()) {
                sendMessageToQueue(requestId, extractedText, service);
            }
        } catch (Exception e) {
            updateStatus(requestId, "Errore: " + e.getMessage());
        }
    }


    
    private String extractTextFromFile(String base64File) {
        RestTemplate restTemplate = new RestTemplate();
    
        // Imposta gli header della richiesta
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        // Crea il body della richiesta come JSON
        String jsonPayload = String.format("{\"file\": \"%s\"}", base64File);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
    
        // Effettua la richiesta POST
        return restTemplate.postForObject(EXTRACT_TEXT_API, request, String.class);
    }
    

    private void sendMessageToQueue(String requestId, String extractedText, String service) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername(RABBITMQ_USER);
        factory.setPassword(RABBITMQ_PASS);
    
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            String queueName = service.equals("summarization") ? QUEUE_SUMMARIZATION : QUEUE_NLP;
            final String correlationId = UUID.randomUUID().toString();
            final String replyQueue = "amq.rabbitmq.reply-to";
    
            // REGISTRA IL CONSUMER PRIMA DI INVIARE IL MESSAGGIO
            final String[] response = new String[1];
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    response[0] = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    updateStatus(requestId, response[0]);
                }
            };
    
            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});
    
            //  SOLO ORA INVIA IL MESSAGGIO
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .messageId(requestId)
                    .replyTo(replyQueue)
                    .build();
    
            String messageToSend = String.format("{ \"text\": \"%s\"}",
                   extractedText);
    
            channel.basicPublish("", queueName, props, messageToSend.getBytes(StandardCharsets.UTF_8));
            while (response[0] == null) {
                Thread.sleep(100);
            }
        }
    }
    


    private void updateStatus(String requestId, String status) {
        System.out.println("Aggiornamento stato - RequestID: " + requestId + " - Stato: " + status);
        // Simulazione aggiornamento in database
        // updateStatusInDatabase(requestId, status);
    }

    public static class RequestPayload {
        private String file;
        private String userId;
        private String fileId;
        private List<String> services;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public List<String> getServices() {
            return services;
        }

        public void setServices(List<String> services) {
            this.services = services;
        }
    }
}
