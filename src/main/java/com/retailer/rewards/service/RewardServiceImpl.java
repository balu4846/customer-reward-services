package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardResponse;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.TransactionNotFoundException;
import com.retailer.rewards.repository.TransactionRepository;
import com.retailer.rewards.util.RewardHelperUtil;
import com.retailer.rewards.validator.RewardRequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardServiceImpl implements RewardService {

    private static final Logger logger = LoggerFactory.getLogger(RewardServiceImpl.class);

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRepository transactionRepository;
    private final RewardRequestValidator validator;

    public RewardServiceImpl(TransactionRepository transactionRepository, RewardRequestValidator validator) {

        this.transactionRepository = transactionRepository;
        this.validator = validator;
    }

    @Override
    public RewardResponse getRewards(Long customerId, LocalDate startDate, LocalDate endDate, Integer months) {

        logger.info("Fetching rewards for customerId={}, startDate={}, endDate={}, months={}",
                customerId,
                startDate,
                endDate,
                months);

        validator.validate(startDate, endDate, months);

        // Default to last 3 months if no date range is provided
        if (startDate == null) {

            int monthsToUse = (months != null) ? months : 3;

            endDate = LocalDate.now();
            startDate = endDate.minusMonths(monthsToUse);

            logger.info("Using calculated date range for customerId={}: startDate={}, endDate={}",
                    customerId,
                    startDate,
                    endDate);
        }
        if (!transactionRepository.existsByCustomerId(customerId)) {
            logger.warn("Customer not found for customerId={}", customerId);
            throw new TransactionNotFoundException("Customer not found: " + customerId);
        }

        List<Transaction>
                transactions =
                transactionRepository.findTransactionsByDateRange(customerId, startDate, endDate);

        if (transactions.isEmpty()) {

            logger.warn("No transactions found for customerId={}, startDate={}, endDate={}",
                    customerId,
                    startDate,
                    endDate);

            throw new TransactionNotFoundException("No transactions found for the given date range");
        }

        logger.info("Found {} transactions for customerId={}", transactions.size(), customerId);

        String customerName = transactions.get(0).getCustomerName();

        Map<String, Double>
                monthlyRewards =
                transactions.stream()
                        .collect(Collectors.groupingBy(transaction -> transaction.getTransactionDate()
                                        .format(MONTH_FORMATTER),
                                Collectors.summingDouble(transaction -> RewardHelperUtil.calculatePoints(transaction.getAmount()))));

        Double totalRewards = monthlyRewards.values().stream().mapToDouble(Double::doubleValue).sum();

        logger.info("Reward calculation completed for customerId={}, totalRewards={}", customerId, totalRewards);

        return new RewardResponse(customerId, customerName, monthlyRewards, totalRewards);
    }
}
