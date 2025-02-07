package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.servercentrale.models.RequestState;
import com.project.servercentrale.models.RequestResults;
import com.project.servercentrale.repositories.RequestStateRepository;
import com.project.servercentrale.repositories.RequestResultsRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class CentralServerController {

    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_USER = "user";
    private static final String RABBITMQ_PASS = "pass";
    private static final String QUEUE_SUMMARIZATION = "summarize_queue";
    private static final String QUEUE_NLP = "nlp_queue";
    private static final String EXTRACT_TEXT_API = "http://localhost:5001/apii/extract/text";
    private static final String UPDATE_STATUS_API = "http://localhost:8080/api/status/update";
    private static final String SAVE_RESULTS_API = "http://localhost:8080/api/results/save";

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private RequestStateRepository requestStateRepository;
    
    @Autowired
    private RequestResultsRepository requestResultsRepository;

    @PostMapping("/process")
    public ResponseEntity<?> handleRequest(@RequestBody RequestPayload payload) {
        String requestId = UUID.randomUUID().toString();

        RequestState requestState = new RequestState(requestId, payload.getUserId(), payload.getFileId(), payload.getServices());
        requestStateRepository.save(requestState);

        executorService.submit(() -> processInBackground(payload, requestId,requestState));
        return ResponseEntity.ok().body("{" + "\"requestId\": \"" + requestId + "\"}" );
    }

    private void processInBackground(RequestPayload payload, String requestId,RequestState requestState) {
        try {
            String extractedText = extractTextFromFile(payload.getFile());
            RequestResults results = new RequestResults(requestId,requestState, extractedText, null, null);
            requestResultsRepository.save(results);

            for (String service : payload.getServices()) {
                sendMessageToQueue(requestId, extractedText, service);
            }
        } catch (Exception e) {
            notifyStatusUpdate(requestId, "error", e.getMessage());
        }
    }

    private String extractTextFromFile(String base64File) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonPayload = String.format("{\"file\": \"%s\"}", base64File);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
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

            final String[] response = new String[1];
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    response[0] = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    saveResults(requestId, service, response[0]);
                    notifyStatusUpdate(requestId, service, "processed");
                }
            };

            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .messageId(requestId)
                    .replyTo(replyQueue)
                    .build();

            String messageToSend = String.format("{ \"text\": \"%s\"}", extractedText);
            channel.basicPublish("", queueName, props, messageToSend.getBytes(StandardCharsets.UTF_8));
            while (response[0] == null) {
                Thread.sleep(100);
            }
        }
    }

    private void notifyStatusUpdate(String requestId, String service, String status) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s?requestId=%s&service=%s&status=%s", UPDATE_STATUS_API, requestId, service, status);
        try {
            restTemplate.postForObject(url, null, String.class);
        } catch (Exception e) {
            System.err.println("Errore nell'aggiornamento dello stato: " + e.getMessage());
        }
    }

    private void saveResults(String requestId, String service, String result) {
        RequestResults results = requestResultsRepository.findById(requestId).get();
        if ("summarization".equals(service)) {
            results.setSummarizationResult(result);
        } else if ("nlp".equals(service)) {
            results.setNlpResult(result);
        }
        requestResultsRepository.save(results);
    }

    public static class RequestPayload {
        private String file;
        private String userId;
        private String fileId;
        private List<String> services;

        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        public List<String> getServices() { return services; }
        public void setServices(List<String> services) { this.services = services; }
    }
}
