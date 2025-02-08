package com.project.servercentrale.strategy;

/**
 * STRATEGY:
 * Definisce il metodo 'process' che ogni strategia (es. Summarization, NLP)
 * deve implementare in modo specifico.
 */
public interface ProcessingStrategy {

    /**
     * Esegue l'elaborazione specifica del servizio (es. Summarization, NLP).
     * @param requestId       l'ID della richiesta
     * @param extractedText   il testo estratto dal file
     */
    void process(String requestId, String extractedText) throws Exception;
}
