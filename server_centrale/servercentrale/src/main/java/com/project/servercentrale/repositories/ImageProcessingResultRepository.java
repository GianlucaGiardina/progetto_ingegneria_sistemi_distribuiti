package com.project.servercentrale.repositories;



import com.project.servercentrale.models.ImageProcessingResult;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageProcessingResultRepository extends MongoRepository<ImageProcessingResult, String> {
}