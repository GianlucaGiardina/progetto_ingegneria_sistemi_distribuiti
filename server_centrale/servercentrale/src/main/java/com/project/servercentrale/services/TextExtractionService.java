package com.project.servercentrale.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servizio dedicato all'estrazione del testo da un file base64,
 * invocando un'API esterna.
 */
@Service
public class TextExtractionService {
    @Value("${extract.text.api}")
    private String EXTRACT_TEXT_API;
    

    public String extractTextFromFile(String base64File) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Costruiamo il JSON
        String jsonPayload = String.format("{\"file\": \"%s\"}", base64File);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        // Chiamata all'API per estrarre il testo
        return restTemplate.postForObject(EXTRACT_TEXT_API, request, String.class);
    }
}
