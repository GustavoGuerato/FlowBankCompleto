package com.flowbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.flowbank.model.Transaction;
import com.flowbank.model.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromUserOrToUserOrderByTimestampDesc(User from, User to);

}
