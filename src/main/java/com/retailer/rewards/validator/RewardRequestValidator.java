package com.retailer.rewards.validator;

import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.exception.InvalidMonthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RewardRequestValidator {

    private static final Logger logger = LoggerFactory.getLogger(RewardRequestValidator.class);

    private static final int MAX_MONTHS = 36;

    public void validate(LocalDate startDate, LocalDate endDate, Integer months) {

        if (startDate == null ^ endDate == null) {

            logger.error(
                    "Validation failed. Both startDate and endDate must be provided together. startDate={}, endDate={}",
                    startDate, endDate);

            throw new InvalidDateRangeException("Both startDate and endDate must be provided together");
        }

        if (startDate != null && months != null) {

            logger.error(
                    "Validation failed. Date range and months cannot be provided together. startDate={}, endDate={}, months={}",
                    startDate, endDate, months);

            throw new InvalidDateRangeException("Provide either date range or months, not both");
        }

        if (months != null && months <= 0) {

            logger.error("Validation failed. Invalid months value: {}", months);

            throw new InvalidMonthException("Months must be greater than 0");
        }

        if (months != null && months > MAX_MONTHS) {
            throw new InvalidMonthException("Months cannot exceed 36");
        }


        if (startDate != null) {

            if (endDate.isBefore(startDate)) {

                logger.error("Validation failed. endDate {} is before startDate {}", endDate, startDate);

                throw new InvalidDateRangeException("endDate cannot be before startDate");
            }

            if (startDate.isAfter(LocalDate.now())) {

                logger.error("Validation failed. startDate {} cannot be in the future", startDate);

                throw new InvalidDateRangeException("startDate cannot be in the future");
            }
        }

        logger.debug("Validation successful. startDate={}, endDate={}, months={}", startDate, endDate, months);
    }
}