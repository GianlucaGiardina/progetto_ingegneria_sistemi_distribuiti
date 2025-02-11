package com.project.servercentrale.strategy;

import com.project.servercentrale.models.PDFProcessingResult;
import com.project.servercentrale.models.ProcessingStatus;
import com.project.servercentrale.repositories.PDFProcessingResultRepository;
import com.project.servercentrale.repositories.ProcessingStatusRepository;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class NlpStrategy implements ProcessingStrategy {

    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_USER = "user";
    private static final String RABBITMQ_PASS = "pass";
    private static final String QUEUE_NLP = "nlp_queue";


    @Autowired
    private PDFProcessingResultRepository pdfProcessingResultRepository;

    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

    @Override
    public void process(String requestId, String extractedText) throws Exception {
        // Stato aggiornato a "in_progress"
        processingStatusRepository.save(new ProcessingStatus(requestId, "nlp", "in_progress"));

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

                    // Recuperiamo il risultato PDF e aggiorniamo il NLP
                    PDFProcessingResult result = pdfProcessingResultRepository.findById(requestId)
                            .orElseThrow(() -> new RuntimeException("PDF Result not found for id: " + requestId));

                    result.setNlpResult(response[0]);
                    pdfProcessingResultRepository.save(result);

                    // Stato aggiornato a "completed"
                    processingStatusRepository.save(new ProcessingStatus(requestId, "nlp", "completed"));
                }
            };

            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueue)
                    .build();

            String messageToSend = String.format("{\"text\":\"%s\"}", extractedText);
            channel.basicPublish("", QUEUE_NLP, props, messageToSend.getBytes(StandardCharsets.UTF_8));

            // Attesa della risposta
            while (response[0] == null) {
                Thread.sleep(100);
            }
        }
    }
}
