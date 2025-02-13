package com.project.servercentrale.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "request_results")
public class ImageProcessingResult extends RequestResults {

    private String contextResult; // Testo generato dall'image-to-text

    public ImageProcessingResult(String requestId, RequestState requestState, String contextResult) {
        super(requestId, requestState);
        this.contextResult = contextResult;
    }

    public String getContextResult() { return contextResult; }

    public void setContextResult(String contextResult) {
        this.contextResult = contextResult;
    }
    
}
