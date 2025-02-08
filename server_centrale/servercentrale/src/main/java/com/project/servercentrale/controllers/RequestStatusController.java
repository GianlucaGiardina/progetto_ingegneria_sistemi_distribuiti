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
