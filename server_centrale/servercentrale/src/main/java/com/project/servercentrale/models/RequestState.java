package com.project.servercentrale.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "requests_state")
public class RequestState {

    @Id
    private String requestId;
    private String userId;
    private String fileId;
    private List<String> services;
    private String summarizationStatus;
    private String nlpStatus;

    public RequestState(String requestId, String userId, String fileId, List<String> services) {
        this.requestId = requestId;
        this.userId = userId;
        this.fileId = fileId;
        this.services = services;
        this.summarizationStatus = services.contains("summarization") ? "pending" : "not_requested";
        this.nlpStatus = services.contains("nlp") ? "pending" : "not_requested";
    }

    public String getRequestId() { return requestId; }
    public String getUserId() { return userId; }
    public String getFileId() { return fileId; }
    public List<String> getServices() { return services; }
    public String getSummarizationStatus() { return summarizationStatus; }
    public String getNlpStatus() { return nlpStatus; }

    public void setSummarizationStatus(String status) { this.summarizationStatus = status; }
    public void setNlpStatus(String status) { this.nlpStatus = status; }
}
