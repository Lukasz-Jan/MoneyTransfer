package com.lj.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.lj.entity.Transaction;

public interface TransactionRepo extends CrudRepository<Transaction, String> {

    @Query(value = "SELECT tr FROM Transaction tr where tr.sa.saId = :id")
    List<Transaction> findBySaId(@Param("id") Long id);
}