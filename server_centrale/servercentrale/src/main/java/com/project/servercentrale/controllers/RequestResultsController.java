package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.servercentrale.models.RequestResults;
import com.project.servercentrale.repositories.RequestResultsRepository;
import java.util.Optional;

@RestController
@RequestMapping("/api/results")
public class RequestResultsController {

    @Autowired
    private RequestResultsRepository requestResultsRepository;



    @GetMapping("/get")
    public ResponseEntity<?> getResults(@RequestParam String requestId) {
        Optional<RequestResults> results = requestResultsRepository.findById(requestId);
        if (results.isPresent()) {
            return ResponseEntity.ok(results.get());
        } else {
            return ResponseEntity.badRequest().body("{" + "\"error\": \"Results not found\"}" );
        }
    }
    
  
    
}
