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

    @PostMapping("/save")
    public ResponseEntity<?> saveResults(@RequestBody RequestResults results) {
        requestResultsRepository.save(results);
        return ResponseEntity.ok().body("{" + "\"message\": \"Results saved successfully\"}" );
    }

    @GetMapping("/get")
    public ResponseEntity<?> getResults(@RequestParam String requestId) {
        Optional<RequestResults> results = requestResultsRepository.findById(requestId);
        if (results.isPresent()) {
            return ResponseEntity.ok(results.get());
        } else {
            return ResponseEntity.badRequest().body("{" + "\"error\": \"Results not found\"}" );
        }
    }
    
    @PostMapping("/update")
    public ResponseEntity<?> updateResults(@RequestParam String requestId, @RequestParam String service, @RequestParam String result) {
        Optional<RequestResults> resultsOpt = requestResultsRepository.findById(requestId);
        if (resultsOpt.isPresent()) {
            RequestResults results = resultsOpt.get();
            if ("summarization".equals(service)) {
                results.setSummarizationResult(result);
            } else if ("nlp".equals(service)) {
                results.setNlpResult(result);
            }
            requestResultsRepository.save(results);
            return ResponseEntity.ok().body("{" + "\"message\": \"Results updated successfully\"}" );
        } else {
            return ResponseEntity.badRequest().body("{" + "\"error\": \"Request ID not found\"}" );
        }
    }
}
