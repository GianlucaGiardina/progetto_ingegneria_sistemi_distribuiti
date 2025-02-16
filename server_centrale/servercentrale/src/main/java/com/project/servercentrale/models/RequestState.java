package com.project.servercentrale.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "requests_state")
public class RequestState {

    @Id
    private String requestId;
    private String userId;
    private String fileId;
    private String fileName;
    private String fileType;  // "pdf" o "image"
    private List<String> services;
    private LocalDateTime createdAt;  // Timestamp di creazione

    public RequestState(String requestId, String userId, String fileId, String fileName, String fileType, List<String> services) {
        this.requestId = requestId;
        this.userId = userId;
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.services = services;
        this.createdAt = LocalDateTime.now();
    }

    public String getRequestId() { return requestId; }
    public String getUserId() { return userId; }
    public String getFileId() { return fileId; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public List<String> getServices() { return services; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
