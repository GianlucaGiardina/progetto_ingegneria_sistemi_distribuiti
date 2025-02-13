package com.project.servercentrale.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

// Indichiamo a Jackson che questa classe ha sottoclassi
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PDFProcessingResult.class, name = "pdf"),
    @JsonSubTypes.Type(value = ImageProcessingResult.class, name = "image")
})
@Document(collection = "request_results")
public abstract class RequestResults {

    @Id
    protected String requestId;

    @DBRef
    protected RequestState requestState;
    
    protected LocalDateTime completedAt;

    public RequestResults(String requestId, RequestState requestState) {
        this.requestId = requestId;
        this.requestState = requestState;
        this.completedAt = LocalDateTime.now();
    }

    public String getRequestId() { return requestId; }
    public RequestState getRequestState() { return requestState; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
