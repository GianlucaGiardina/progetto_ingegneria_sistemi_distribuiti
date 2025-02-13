package com.project.servercentrale.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.servercentrale.models.ProcessingStatus;
import com.project.servercentrale.models.RequestState;
import com.project.servercentrale.repositories.ProcessingStatusRepository;
import com.project.servercentrale.repositories.RequestStateRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/status")
public class RequestStatusController {

    @Autowired
    private RequestStateRepository requestStateRepository;
    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

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

    if (requests.isEmpty()) {
        return ResponseEntity.badRequest().body("{\"error\": \"No requests found for this user\"}");
    }

    // Lista per raccogliere le risposte con lo stato dei servizi
    List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();

    for (RequestState request : requests) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requestId", request.getRequestId());
        requestData.put("fileId", request.getFileId());
        requestData.put("fileType", request.getFileType());
        requestData.put("services", request.getServices());
        requestData.put("createdAt", request.getCreatedAt());

        // Recuperiamo gli stati dei servizi associati alla richiesta
        List<ProcessingStatus> statuses = processingStatusRepository.findByRequestId(request.getRequestId());
        Map<String, String> serviceStatuses = new HashMap<>();
        for (ProcessingStatus status : statuses) {
            serviceStatuses.put(status.getServiceType(), status.getStatus());
        }

        requestData.put("serviceStatuses", serviceStatuses);

        responseList.add(requestData);
    }

    return ResponseEntity.ok(responseList);
}

}
