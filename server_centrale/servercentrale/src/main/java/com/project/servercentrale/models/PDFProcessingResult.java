package com.project.servercentrale.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "request_results")
public class PDFProcessingResult extends RequestResults {

    private String extractedText;
    private String summarizationResult;
    private String nlpResult;

    public PDFProcessingResult(String requestId, RequestState requestState, String extractedText, String summarizationResult, String nlpResult) {
        super(requestId, requestState);
        this.extractedText = extractedText;
        this.summarizationResult = summarizationResult;
        this.nlpResult = nlpResult;
    }

    public String getExtractedText() { return extractedText; }
    public String getSummarizationResult() { return summarizationResult; }
    public String getNlpResult() { return nlpResult; }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public void setSummarizationResult(String summarizationResult) {
        this.summarizationResult = summarizationResult;
    }

    public void setNlpResult(String nlpResult) {
        this.nlpResult = nlpResult;
    }
    
}
