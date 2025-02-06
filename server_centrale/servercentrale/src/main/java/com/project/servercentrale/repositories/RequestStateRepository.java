package com.project.servercentrale.repositories;


import com.project.servercentrale.models.RequestState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestStateRepository extends MongoRepository<RequestState, String> {
}
