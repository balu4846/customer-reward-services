package com.retailer.rewards.controller;

import com.retailer.rewards.dto.RewardResponse;
import com.retailer.rewards.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class RewardController {

    private static final Logger logger = LoggerFactory.getLogger(RewardController.class);

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Operation(summary = "Get Customer Reward Points", description = "Fetch customer reward points for a customer using either a date range or number of months")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Rewards fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid request parameters"), @ApiResponse(responseCode = "404", description = "No transactions found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/rewards/{customerId}")
    public RewardResponse getRewards(

            @Parameter(description = "Customer ID") @PathVariable Long customerId,

            @Parameter(description = "Start date in yyyy-MM-dd format") @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "End date in yyyy-MM-dd format") @RequestParam(required = false) LocalDate endDate,

            @Parameter(description = "Number of months (1-36)") @RequestParam(required = false) Integer months) {

        logger.info("Received reward request for customerId={}, startDate={}, endDate={}, months={}",
                customerId,
                startDate,
                endDate,
                months);

        RewardResponse response = rewardService.getRewards(customerId,
                startDate,
                endDate,
                months);

        logger.info("Rewards fetched successfully for customerId={}",
                customerId);

        return response;
    }
}