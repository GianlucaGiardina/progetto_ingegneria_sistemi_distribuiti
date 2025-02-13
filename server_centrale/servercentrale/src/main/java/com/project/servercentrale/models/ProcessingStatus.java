package com.project.servercentrale.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "processing_status")
public class ProcessingStatus {

    @Id
    private String statusId;  // ID univoco generato automaticamente da MongoDB

    @Indexed
    private String requestId; // ID della richiesta principale

    private String serviceType;  // "summarization", "nlp", "context"
    private String status;  // "pending", "in_progress", "completed", "failed"
    private LocalDateTime updatedAt;

    public ProcessingStatus(String requestId, String serviceType, String status) {
        this.requestId = requestId;
        this.serviceType = serviceType;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatusId() { return statusId; }
    public String getRequestId() { return requestId; }
    public String getServiceType() { return serviceType; }
    public String getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
