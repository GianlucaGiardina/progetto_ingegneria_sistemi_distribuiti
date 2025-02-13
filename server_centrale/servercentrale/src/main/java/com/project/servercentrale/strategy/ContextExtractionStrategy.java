package com.project.servercentrale.strategy;

import com.project.servercentrale.models.ImageProcessingResult;
import com.project.servercentrale.models.ProcessingStatus;
import com.project.servercentrale.models.RequestState;
import com.project.servercentrale.repositories.ImageProcessingResultRepository;
import com.project.servercentrale.repositories.ProcessingStatusRepository;
import com.project.servercentrale.repositories.RequestStateRepository;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class ContextExtractionStrategy implements ProcessingStrategy {

@Value("${rabbitmq.host}")
private String RABBITMQ_HOST;
@Value("${rabbitmq.user}")
private String RABBITMQ_USER;
@Value("${rabbitmq.pass}")
private String RABBITMQ_PASS;
@Value("${rabbitmq.queue.context}")
private String QUEUE_CONTEXT;
    @Autowired
    private ImageProcessingResultRepository imageProcessingResultRepository;
    @Autowired
    private RequestStateRepository requestStateRepository;

    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

    @Override
    public void process(String requestId, String imageBase64) throws Exception {
        // Stato aggiornato a "in_progress"
        ProcessingStatus processingStatus =new ProcessingStatus(requestId, "context", "in_progress");
        processingStatusRepository.save(processingStatus);

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

                    RequestState rs =requestStateRepository.findById(processingStatus.getRequestId()).get();
                    ImageProcessingResult result = new ImageProcessingResult(
                            processingStatus.getRequestId(),
                            rs,
                            response[0]
                    );
                    imageProcessingResultRepository.save(result);
                    // Stato aggiornato a "completed"
                    processingStatusRepository.save(new ProcessingStatus(requestId, "context", "completed"));
                }
            };

            channel.basicConsume(replyQueue, true, deliverCallback, consumerTag -> {});

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueue)
                    .build();

            channel.basicPublish("", QUEUE_CONTEXT, props, imageBase64.getBytes(StandardCharsets.UTF_8));

            // Attesa della risposta
            while (response[0] == null) {
                Thread.sleep(100);
            }
        }
    }
}
