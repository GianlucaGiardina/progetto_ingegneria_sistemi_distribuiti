package com.project.servercentrale.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "request_results")
public class RequestResults {

    @Id
    private String requestId;
    
    @DBRef
    private RequestState requestState;
    
    private String extractedText;
    private String summarizationResult;
    private String nlpResult;

    public RequestResults(String requestId, RequestState requestState, String extractedText, String summarizationResult, String nlpResult) {
        this.requestId = requestId;
        this.requestState = requestState;
        this.extractedText = extractedText;
        this.summarizationResult = summarizationResult;
        this.nlpResult = nlpResult;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public RequestState getRequestState() { return requestState; }
    public void setRequestState(RequestState requestState) { this.requestState = requestState; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getSummarizationResult() { return summarizationResult; }
    public void setSummarizationResult(String summarizationResult) { this.summarizationResult = summarizationResult; }

    public String getNlpResult() { return nlpResult; }
    public void setNlpResult(String nlpResult) { this.nlpResult = nlpResult; }
}
