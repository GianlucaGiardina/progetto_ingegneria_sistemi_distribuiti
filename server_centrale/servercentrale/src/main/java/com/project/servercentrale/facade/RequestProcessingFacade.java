package com.project.servercentrale.facade;

import com.project.servercentrale.controllers.CentralServerController.RequestPayload;
import com.project.servercentrale.models.RequestResults;
import com.project.servercentrale.models.RequestState;
import com.project.servercentrale.repositories.RequestResultsRepository;
import com.project.servercentrale.repositories.RequestStateRepository;
import com.project.servercentrale.services.TextExtractionService;
import com.project.servercentrale.strategy.ProcessingStrategy;
import com.project.servercentrale.strategy.StrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FACADE:
 * Classe che nasconde la complessità dell'intero processo di "processRequest".
 * Il Controller invoca un solo metodo, e tutta la logica è incapsulata qui.
 */
@Service
public class RequestProcessingFacade {

    @Autowired
    private RequestStateRepository requestStateRepository;
    @Autowired
    private RequestResultsRepository requestResultsRepository;
    @Autowired
    private TextExtractionService textExtractionService;
    @Autowired
    private StrategyFactory strategyFactory;

    // Gestione thread pool per l'esecuzione in background
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Metodo principale (entry point) invocato dal Controller.
     */
    public String processRequest(RequestPayload payload) {
        // 1) Creiamo un nuovo requestId e salviamo lo stato iniziale
        String requestId = UUID.randomUUID().toString();
        RequestState requestState = new RequestState(
                requestId,
                payload.getUserId(),
                payload.getFileId(),
                payload.getServices()
        );
        requestStateRepository.save(requestState);

        // 2) Avviamo l'elaborazione asincrona
        executorService.submit(() -> processInBackground(payload, requestState));

        // 3) Restituiamo il requestId al chiamante
        return requestId;
    }

    /**
     * Metodo privato che gira in un thread separato
     */
    private void processInBackground(RequestPayload payload, RequestState requestState) {
        try {
            // 1) Estrazione testo
            String extractedText = textExtractionService.extractTextFromFile(payload.getFile());

            // 2) Salvataggio risultati iniziali (contenente il testo estratto)
            RequestResults results = new RequestResults(
                    requestState.getRequestId(),
                    requestState,
                    extractedText,
                    null,
                    null
            );
            requestResultsRepository.save(results);

            // 3) Per ogni servizio, recuperiamo la STRATEGY corrispondente
            for (String serviceName : payload.getServices()) {
                // StrategyFactory ci restituisce la Strategy giusta
                ProcessingStrategy strategy = strategyFactory.getStrategy(serviceName);

                // Usiamo la Strategy per eseguire la logica
                strategy.process(requestState.getRequestId(), extractedText);
            }

        } catch (Exception e) {
            // Se c'è errore, aggiorniamo manualmente lo stato come "error"
            // (oppure potremmo avere una strategy "error" se volessimo)
            requestState.setSummarizationStatus("error: " + e.getMessage());
            requestState.setNlpStatus("error: " + e.getMessage());
            requestStateRepository.save(requestState);
        }
    }
}
