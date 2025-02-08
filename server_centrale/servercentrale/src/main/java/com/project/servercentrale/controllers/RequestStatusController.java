package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.servercentrale.models.RequestState;
import com.project.servercentrale.repositories.RequestStateRepository;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/status")
public class RequestStatusController {

    @Autowired
    private RequestStateRepository requestStateRepository;

    @PostMapping("/update")
    public ResponseEntity<?> updateStatus(@RequestParam String requestId, @RequestParam String service, @RequestParam String status) {
        System.out.println(requestId + " " + status + " " + service);
        RequestState requestState = requestStateRepository.findById(requestId).orElse(null);
        if (requestState != null) {
            if ("summarization".equals(service)) {
                requestState.setSummarizationStatus(status);
            } else if ("nlp".equals(service)) {
                requestState.setNlpStatus(status);
            }
            requestStateRepository.save(requestState);
            return ResponseEntity.ok().body("{" + "\"message\": \"Status updated successfully\"}" );
        } else {
            return ResponseEntity.badRequest().body("{" + "\"error\": \"Request ID not found\"}" );
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getStatus(@RequestParam String requestId) {
        Optional<RequestState> requestState = requestStateRepository.findById(requestId);
        if (requestState.isPresent()) {
            return ResponseEntity.ok(requestState.get());
        } else {
            return ResponseEntity.badRequest().body("{" + "\"error\": \"Request ID not found\"}" );
        }
    }
    
    @GetMapping("/getByUserId")
    public ResponseEntity<?> getRequestsByUserId(@RequestParam String userId) {
        List<RequestState> requests = requestStateRepository.findByUserId(userId);
        if (!requests.isEmpty()) {
            return ResponseEntity.ok(requests);
        } else {
            return ResponseEntity.badRequest().body("{" + "\"error\": \"No requests found for this user\"}" );
        }
    }
}
