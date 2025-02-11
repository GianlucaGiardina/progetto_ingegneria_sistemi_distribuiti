package com.project.servercentrale.repositories;

    

import com.project.servercentrale.models.PDFProcessingResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PDFProcessingResultRepository extends MongoRepository<PDFProcessingResult, String> {
}