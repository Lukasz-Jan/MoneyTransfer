package com.lj.mongo.repository;

import com.lj.mongo.repository.documents.AccountDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepo extends MongoRepository<AccountDoc, String> {

}
