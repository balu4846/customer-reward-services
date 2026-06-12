package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardResponse;

import java.time.LocalDate;

public interface RewardService {
    RewardResponse getRewards(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate,
            Integer months);
}
