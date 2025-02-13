package com.project.servercentrale.repositories;



    

import com.project.servercentrale.models.ProcessingStatus;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingStatusRepository extends MongoRepository<ProcessingStatus, String> {

    List<ProcessingStatus> findByRequestId(String requestId);
}