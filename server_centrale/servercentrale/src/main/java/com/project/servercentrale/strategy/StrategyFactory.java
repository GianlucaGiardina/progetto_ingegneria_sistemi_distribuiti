package com.project.servercentrale.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * FACTORY che restituisce la Strategy corretta in base al servizio richiesto.
 */
@Component
public class StrategyFactory {

    @Autowired
    private SummarizationStrategy summarizationStrategy;
    @Autowired
    private NlpStrategy nlpStrategy;

    public ProcessingStrategy getStrategy(String serviceName) {
        if ("summarization".equalsIgnoreCase(serviceName)) {
            return summarizationStrategy;
        } else if ("nlp".equalsIgnoreCase(serviceName)) {
            return nlpStrategy;
        } else {
            // In caso di servizio sconosciuto, potresti lanciare eccezione
            throw new IllegalArgumentException("Servizio sconosciuto: " + serviceName);
        }
    }
}
