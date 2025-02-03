package com.project.servercentrale.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class CentralServerController {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/process")
    public ResponseEntity<?> handleRequest(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("fileId") String fileId,
            @RequestParam("services") List<String> services) {

        // Salva il file
        String filePath;
        try {
            filePath = saveFile(file, fileId);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Errore nel salvataggio del file: " + e.getMessage());
        }

        // Restituisci subito la conferma al client
        Map<String, String> initialResponse = new HashMap<>();
        initialResponse.put("message", "Richiesta ricevuta");
        initialResponse.put("userId", userId);
        initialResponse.put("fileId", fileId);

        // Elabora i servizi in background
        executorService.submit(() -> processServices(filePath, userId, fileId, services));

        return ResponseEntity.ok(initialResponse);
    }

    private void processServices(String filePath, String userId, String fileId, List<String> services) {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("userId", userId);
        statusUpdate.put("fileId", fileId);

        if (services.contains("summarization")) {
            String summaryResult = callSummarizationService(filePath);
            updateStatus(userId, fileId, "summarization", "completed", summaryResult);
        }
        if (services.contains("nlp")) {
            String nlpResult = callNlpService(filePath);
            updateStatus(userId, fileId, "nlp", "completed", nlpResult);
        }
    }

    private String saveFile(MultipartFile file, String fileId) throws IOException {
        // Salva il file su disco o storage
        String filePath = "/path/to/uploads/" + fileId + ".pdf";
        file.transferTo(new File(filePath));
        return filePath;
    }

    private String callSummarizationService(String filePath) {
        // Invoca il servizio di Summarization
        // Simulazione chiamata REST o logica locale
        return "Risultato della Summarization per il file: " + filePath;
    }

    private String callNlpService(String filePath) {
        // Invoca il servizio NLP
        // Simulazione chiamata REST o logica locale
        return "Risultato dell'NLP per il file: " + filePath;
    }

    private void updateStatus(String userId, String fileId, String service, String status, String result) {
        // Simula un aggiornamento dello stato del servizio in una tabella o log
        System.out.println("Aggiornamento stato - UserID: " + userId + ", FileID: " + fileId + ", Service: " + service + ", Status: " + status + ", Result: " + result);
        // Puoi usare un database per aggiornare lo stato delle richieste
    }
}
