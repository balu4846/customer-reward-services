package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardResponse;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.TransactionNotFoundException;
import com.retailer.rewards.repository.TransactionRepository;
import com.retailer.rewards.validator.RewardRequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RewardRequestValidator validator;

    @InjectMocks
    private RewardServiceImpl rewardService;

    @Test
    void shouldCalculateRewardsSuccessfully() {

        Long customerId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        List<Transaction> transactions =
                Arrays.asList(new Transaction(1L, customerId, "John", new BigDecimal("120"), LocalDate.of(2025, 1, 15)),

                        new Transaction(2L, customerId, "John", new BigDecimal("75"), LocalDate.of(2025, 1, 25)));
        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                eq(startDate),
                eq(endDate))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, startDate, endDate, null);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
        assertEquals(115.0, response.getTotalRewards());
        assertEquals(115.0, response.getMonthlyRewards().get("2025-01"));

        verify(validator).validate(startDate, endDate, null);
    }

    @Test
    void shouldThrowExceptionWhenNoTransactionsFound() {

        Long customerId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                eq(startDate),
                eq(endDate))).thenReturn(Collections.emptyList());

        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
                () -> rewardService.getRewards(customerId, startDate, endDate, null));

        assertEquals("No transactions found for the given date range", exception.getMessage());

        verify(validator).validate(startDate, endDate, null);
    }

    @Test
    void shouldGroupRewardsByMonth() {

        Long customerId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        List<Transaction> transactions = Arrays.asList(

                new Transaction(1L, customerId, "John", new BigDecimal("120"), LocalDate.of(2025, 1, 10)),

                new Transaction(2L, customerId, "John", new BigDecimal("80"), LocalDate.of(2025, 2, 10)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                eq(startDate),
                eq(endDate))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, startDate, endDate, null);

        assertEquals(90.0, response.getMonthlyRewards().get("2025-01"));
        assertEquals(30.0, response.getMonthlyRewards().get("2025-02"));
        assertEquals(120.0, response.getTotalRewards());
        assertEquals(2, response.getMonthlyRewards().size());
    }

    @Test
    void shouldReturnZeroRewardsForAmountExactly50() {

        Long customerId = 1L;

        List<Transaction> transactions =
                List.of(new Transaction(1L, customerId, "John", new BigDecimal("50"), LocalDate.of(2025, 1, 10)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                any(LocalDate.class),
                any(LocalDate.class))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, null, null, null);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
        assertEquals(0.0, response.getTotalRewards());
        assertEquals(0.0, response.getMonthlyRewards().get("2025-01"));
    }

    @Test
    void shouldReturn50RewardsForAmountExactly100() {

        Long customerId = 1L;

        List<Transaction> transactions =
                List.of(new Transaction(1L, customerId, "John", new BigDecimal("100"), LocalDate.of(2025, 1, 10)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                any(LocalDate.class),
                any(LocalDate.class))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, null, null, null);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
        assertEquals(50.0, response.getTotalRewards());
        assertEquals(50.0, response.getMonthlyRewards().get("2025-01"));
    }

    @Test
    void shouldCalculateRewardsForDecimalAmount() {

        Long customerId = 1L;

        List<Transaction> transactions =
                List.of(new Transaction(1L, customerId, "John", new BigDecimal("120.50"), LocalDate.of(2025, 1, 10)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                any(LocalDate.class),
                any(LocalDate.class))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, null, null, null);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
        assertEquals(91.0, response.getTotalRewards());
        assertEquals(91.0, response.getMonthlyRewards().get("2025-01"));
    }

    @Test
    void shouldUseDefaultThreeMonthsWhenDatesAndMonthsAreNull() {

        Long customerId = 1L;

        List<Transaction> transactions =
                List.of(new Transaction(1L, customerId, "John", new BigDecimal("120"), LocalDate.now().minusDays(10)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                any(LocalDate.class),
                any(LocalDate.class))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, null, null, null);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
        assertEquals(90.0, response.getTotalRewards());

        verify(validator).validate(null, null, null);
    }

    @Test
    void shouldUseProvidedMonthsParameter() {

        Long customerId = 1L;

        List<Transaction> transactions =
                List.of(new Transaction(1L, customerId, "John", new BigDecimal("120"), LocalDate.now().minusDays(10)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                any(LocalDate.class),
                any(LocalDate.class))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, null, null, 6);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
        assertEquals(90.0, response.getTotalRewards());

        verify(validator).validate(null, null, 6);
    }

    @Test
    void shouldSumRewardsForTransactionsInSameMonth() {

        Long customerId = 1L;

        List<Transaction> transactions =
                List.of(new Transaction(1L, customerId, "John", new BigDecimal("120"), LocalDate.of(2025, 1, 10)),

                        new Transaction(2L, customerId, "John", new BigDecimal("120"), LocalDate.of(2025, 1, 20)));

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(true);

        when(transactionRepository.findTransactionsByDateRange(eq(customerId),
                any(LocalDate.class),
                any(LocalDate.class))).thenReturn(transactions);

        RewardResponse response = rewardService.getRewards(customerId, null, null, null);

        assertEquals(180.0, response.getMonthlyRewards().get("2025-01"));

        assertEquals(180.0, response.getTotalRewards());
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionWhenCustomerDoesNotExist() {

        Long customerId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        when(transactionRepository.existsByCustomerId(eq(customerId))).thenReturn(false);

        TransactionNotFoundException
                exception =
                assertThrows(TransactionNotFoundException.class,
                        () -> rewardService.getRewards(customerId, startDate, endDate, null));

        assertEquals("Customer not found: " + customerId, exception.getMessage());

        verify(validator).validate(startDate, endDate, null);
    }
}