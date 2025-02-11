package com.project.servercentrale.controllers;

import com.project.servercentrale.facade.RequestProcessingFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CentralServerController {

    @Autowired
    private RequestProcessingFacade requestProcessingFacade;

    /**
     * API per processare file PDF (Summarization / NLP)
     */
    @PostMapping("/process_pdf")
    public ResponseEntity<?> handlePdfRequest(@RequestBody RequestPayload payload) {
        if (payload.getFile() == null || payload.getServices().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"File e servizi sono obbligatori\"}");
        }
        String requestId = requestProcessingFacade.processPdfRequest(payload);
        return ResponseEntity.ok().body("{\"requestId\":\"" + requestId + "\"}");
    }

    /**
     * API per processare immagini (Context Extraction)
     */
    @PostMapping("/process_image")
    public ResponseEntity<?> handleImageRequest(@RequestBody RequestPayload payload) {
        if (payload.getFile() == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"File immagine obbligatorio\"}");
        }
        String requestId = requestProcessingFacade.processImageRequest(payload);
        return ResponseEntity.ok().body("{\"requestId\":\"" + requestId + "\"}");
    }

    /**
     * Classe Payload (uguale per PDF e immagini)
     */
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
