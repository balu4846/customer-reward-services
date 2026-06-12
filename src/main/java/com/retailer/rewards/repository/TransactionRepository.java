package com.retailer.rewards.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.retailer.rewards.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("SELECT t FROM Transaction t "
			+ "WHERE t.customerId = :customerId "
			+ "AND t.transactionDate BETWEEN :startDate AND :endDate")
	List<Transaction> findTransactionsByDateRange(
			@Param("customerId") Long customerId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	boolean existsByCustomerId(Long customerId);

}
