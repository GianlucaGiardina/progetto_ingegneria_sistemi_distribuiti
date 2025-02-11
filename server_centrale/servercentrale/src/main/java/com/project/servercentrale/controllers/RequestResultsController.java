package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.servercentrale.models.RequestResults;
import com.project.servercentrale.repositories.RequestResultsRepository;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class RequestResultsController {

    @Autowired
    private RequestResultsRepository requestResultsRepository;



    @GetMapping("/get")
    public ResponseEntity<?> getResults(@RequestParam String requestId) {
        List<RequestResults> results = requestResultsRepository.findAllByRequestId(requestId);
    
        if (!results.isEmpty()) {
            return ResponseEntity.ok(results);
        } else {
            return ResponseEntity.badRequest().body("{\"error\": \"Results not found\"}");
        }
    }
    
    
  
    
}
