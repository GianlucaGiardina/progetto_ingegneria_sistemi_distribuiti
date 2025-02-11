package com.project.servercentrale.facade;

import com.project.servercentrale.controllers.CentralServerController.RequestPayload;
import com.project.servercentrale.models.*;
import com.project.servercentrale.repositories.*;
import com.project.servercentrale.services.TextExtractionService;
import com.project.servercentrale.strategy.ProcessingStrategy;
import com.project.servercentrale.strategy.StrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class RequestProcessingFacade {

    @Autowired
    private RequestStateRepository requestStateRepository;

    @Autowired
    private PDFProcessingResultRepository pdfProcessingResultRepository;

    @Autowired
    private ImageProcessingResultRepository imageProcessingResultRepository;

    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

    @Autowired
    private TextExtractionService textExtractionService;

    @Autowired
    private StrategyFactory strategyFactory;

    /**
     * Processa una richiesta PDF e la invia a Summarization / NLP.
     */
/**
 * Processa una richiesta PDF e la invia a Summarization / NLP.
 */
public String processPdfRequest(RequestPayload payload) {
    String requestId = UUID.randomUUID().toString();

    // 1) Salviamo lo stato iniziale della richiesta
    RequestState requestState = new RequestState(
            requestId,
            payload.getUserId(),
            payload.getFileId(),
            "pdf",
            payload.getServices()
    );
    requestStateRepository.save(requestState);

    // 2) Elaborazione asincrona del PDF
    CompletableFuture.supplyAsync(() -> textExtractionService.extractTextFromFile(payload.getFile()))
    .thenApply(extractedText -> {
        PDFProcessingResult results = new PDFProcessingResult(
                requestState.getRequestId(),
                requestState,
                extractedText,
                null,
                null
        );
        pdfProcessingResultRepository.save(results);
        return extractedText;
    })
    .thenAccept(extractedText -> {
        List<CompletableFuture<Void>> strategyFutures = new ArrayList<>();

        for (String serviceName : payload.getServices()) {
            ProcessingStrategy strategy = strategyFactory.getStrategy(serviceName);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    strategy.process(requestId, extractedText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            strategyFutures.add(future);

            // Aggiorniamo lo stato a "in_progress"
            processingStatusRepository.save(new ProcessingStatus(requestId, serviceName, "in_progress"));
        }

        CompletableFuture<Void> allDone = CompletableFuture.allOf(strategyFutures.toArray(new CompletableFuture[0]));
        allDone.whenComplete((ignored, ex) -> {
            if (ex != null) {
                for (String serviceName : payload.getServices()) {
                    processingStatusRepository.save(new ProcessingStatus(requestId, serviceName, "failed"));
                }
            }
        });
    })
    .exceptionally(ex -> {
        for (String serviceName : payload.getServices()) {
            processingStatusRepository.save(new ProcessingStatus(requestId, serviceName, "failed"));
        }
        return null;
    });

    return requestId;
}
/**
 * Processa una richiesta immagine e la invia a Context Extraction.
 */
public String processImageRequest(RequestPayload payload) {
    String requestId = UUID.randomUUID().toString();

    // 1) Salviamo lo stato iniziale della richiesta
    RequestState requestState = new RequestState(
            requestId,
            payload.getUserId(),
            payload.getFileId(),
            "image",
            payload.getServices()
    );
    requestStateRepository.save(requestState);

    // 2) Avviamo l'elaborazione asincrona dell'immagine con la Strategy corretta
    List<CompletableFuture<Void>> strategyFutures = new ArrayList<>();

    for (String serviceName : payload.getServices()) {
        ProcessingStrategy strategy = strategyFactory.getStrategy(serviceName);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                strategy.process(requestId, payload.getFile());
            } catch (Exception e) {
                e.printStackTrace();
                processingStatusRepository.save(new ProcessingStatus(requestId, serviceName, "failed"));
            }
        });

        strategyFutures.add(future);
        processingStatusRepository.save(new ProcessingStatus(requestId, serviceName, "in_progress"));
    }

    // 3) Attendi il completamento di tutte le strategie
    CompletableFuture<Void> allDone = CompletableFuture.allOf(strategyFutures.toArray(new CompletableFuture[0]));
    allDone.whenComplete((ignored, ex) -> {
        if (ex != null) {
            for (String serviceName : payload.getServices()) {
                processingStatusRepository.save(new ProcessingStatus(requestId, serviceName, "failed"));
            }
        } else {
            processingStatusRepository.save(new ProcessingStatus(requestId, "context", "completed"));
        }
    });

    return requestId;
}


    
}
