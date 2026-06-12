package com.retailer.rewards.util;

import java.math.BigDecimal;


public class RewardHelperUtil {


    /**
     * Calculate reward points based on transaction amount.
     * <p>
     * Rules:
     * - No points for first 50
     * - 1 point for each dollar between 50 and 100
     * - 2 points for each dollar above 100
     *
     * @param amount transaction amount
     * @return calculated reward points
     */
    public static double calculatePoints(BigDecimal amount) {

        double points = 0;

        if (amount.compareTo(BigDecimal.valueOf(100)) > 0) {

            BigDecimal above100 = amount.subtract(BigDecimal.valueOf(100));
            points += above100.multiply(BigDecimal.valueOf(2)).doubleValue();

            points += 50;

        } else if (amount.compareTo(BigDecimal.valueOf(50)) > 0) {

            BigDecimal above50 = amount.subtract(BigDecimal.valueOf(50));
            points += above50.doubleValue();
        }

        return points;
    }

}
