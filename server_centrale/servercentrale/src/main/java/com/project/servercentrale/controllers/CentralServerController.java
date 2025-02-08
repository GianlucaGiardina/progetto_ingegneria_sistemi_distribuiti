package com.project.servercentrale.controllers;

import com.project.servercentrale.facade.RequestProcessingFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Il Controller rimane molto leggero
@RestController
@RequestMapping("/api")
public class CentralServerController {

    @Autowired
    private RequestProcessingFacade requestProcessingFacade;

    /**
     * Riceve la richiesta di processo e delega al Facade.
     */
    @PostMapping("/process")
    public ResponseEntity<?> handleRequest(@RequestBody RequestPayload payload) {
        String requestId = requestProcessingFacade.processRequest(payload);
        return ResponseEntity.ok().body("{\"requestId\":\"" + requestId + "\"}");
    }

    // Se avevi altri endpoint (es. /update), puoi mantenerli qui
    // o spostarli dove preferisci.

    // DTO/Request payload del corpo JSON
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
