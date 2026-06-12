package com.retailer.rewards.controller;

import com.retailer.rewards.dto.RewardResponse;
import com.retailer.rewards.exception.InvalidMonthException;
import com.retailer.rewards.exception.TransactionNotFoundException;
import com.retailer.rewards.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardService rewardService;

    @Test
    void shouldReturnRewardsSuccessfully() throws Exception {

        Map<String, Double> monthlyRewards = new HashMap<>();
        monthlyRewards.put("JANUARY", 90.0);
        monthlyRewards.put("FEBRUARY", 120.0);

        RewardResponse response = new RewardResponse();
        response.setCustomerId(101L);
        response.setCustomerName("John");
        response.setMonthlyRewards(monthlyRewards);
        response.setTotalRewards(210.0);

        when(rewardService.getRewards(101L, null, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/rewards/101")).andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(101)).andExpect(jsonPath("$.customerName").value("John"))
                .andExpect(jsonPath("$.totalRewards").value(210.0))
                .andExpect(jsonPath("$.monthlyRewards.JANUARY").value(90.0))
                .andExpect(jsonPath("$.monthlyRewards.FEBRUARY").value(120.0));

        verify(rewardService).getRewards(101L, null, null, null);
    }

    @Test
    void shouldPassMonthsParameterToService() throws Exception {

        RewardResponse response = new RewardResponse();
        response.setCustomerId(101L);
        response.setCustomerName("John");
        response.setTotalRewards(90.0);

        when(rewardService.getRewards(101L, null, null, 3)).thenReturn(response);

        mockMvc.perform(get("/api/rewards/101").param("months", "3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(101)).andExpect(jsonPath("$.customerName").value("John"))
                .andExpect(jsonPath("$.totalRewards").value(90.0));

        verify(rewardService).getRewards(101L, null, null, 3);
    }

    @Test
    void shouldPassDateRangeToService() throws Exception {

        RewardResponse response = new RewardResponse();
        response.setCustomerId(101L);
        response.setCustomerName("John");
        response.setTotalRewards(115.0);

        when(rewardService.getRewards(101L, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-03-31"),
                null)).thenReturn(response);

        mockMvc.perform(get("/api/rewards/101").param("startDate", "2025-01-01").param("endDate", "2025-03-31"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.customerId").value(101))
                .andExpect(jsonPath("$.customerName").value("John")).andExpect(jsonPath("$.totalRewards").value(115.0));

        verify(rewardService).getRewards(101L, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-03-31"), null);
    }

    @Test
    void shouldReturnBadRequestWhenMonthsExceedsMaximumLimit() throws Exception {

        when(rewardService.getRewards(eq(101L), isNull(), isNull(), eq(37))).thenThrow(
                new InvalidMonthException("Months cannot exceed 36"));

        mockMvc.perform(get("/api/rewards/101").param("months", "37")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Months cannot exceed 36"));
    }

    @Test
    void shouldReturnNotFoundWhenTransactionsNotExist() throws Exception {

        when(rewardService.getRewards(eq(101L), isNull(), isNull(), isNull())).thenThrow(
                new TransactionNotFoundException("No transactions found for the given date range"));

        mockMvc.perform(get("/api/rewards/101")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No transactions found for the given date range"));
    }

    @Test
    void shouldReturnBadRequestForInvalidDateFormat() throws Exception {

        mockMvc.perform(get("/api/rewards/101").param("startDate", "invalid-date").param("endDate", "2025-03-31"))
                .andExpect(status().isBadRequest());
    }
}