package com.project.servercentrale.repositories;




import com.project.servercentrale.models.RequestResults;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestResultsRepository extends MongoRepository<RequestResults, String> {

    List<RequestResults> findAllByRequestId(String requestId);


}