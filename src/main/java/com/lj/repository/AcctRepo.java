package com.lj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.lj.entity.*;

@Repository
public interface AcctRepo extends CrudRepository<Account, String> {



    @Query(value = "SELECT acc FROM Account acc JOIN FETCH acc.agreements where acc.acctId = :id")
    Optional<Account> findById(@Param("id") String id);

    @Query(value = "SELECT acc FROM Account acc JOIN FETCH acc.agreements")
    Iterable<Account> findAll();
}