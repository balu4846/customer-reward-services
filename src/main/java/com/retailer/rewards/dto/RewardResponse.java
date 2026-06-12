package com.retailer.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardResponse {

    private Long customerId;

    private String customerName;

    private Map<String, Double> monthlyRewards;

    private Double totalRewards;

}
