package com.retailer.rewards.validator;

import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.exception.InvalidMonthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RewardRequestValidatorTest {

    private RewardRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RewardRequestValidator();
    }

    @Test
    void shouldThrowExceptionWhenOnlyStartDateProvided() {

        LocalDate startDate = LocalDate.now();

        assertThrows(InvalidDateRangeException.class, () -> validator.validate(startDate, null, null));
    }

    @Test
    void shouldThrowExceptionWhenOnlyEndDateProvided() {

        LocalDate endDate = LocalDate.now();

        assertThrows(InvalidDateRangeException.class, () -> validator.validate(null, endDate, null));
    }

    @Test
    void shouldThrowExceptionWhenDateRangeAndMonthsProvided() {

        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        assertThrows(InvalidDateRangeException.class, () -> validator.validate(startDate, endDate, 3));
    }

    @Test
    void shouldThrowExceptionWhenMonthsIsZero() {

        assertThrows(InvalidMonthException.class, () -> validator.validate(null, null, 0));
    }

    @Test
    void shouldThrowExceptionWhenMonthsIsNegative() {

        assertThrows(InvalidMonthException.class, () -> validator.validate(null, null, -2));
    }

    @Test
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        assertThrows(InvalidDateRangeException.class, () -> validator.validate(startDate, endDate, null));
    }

    @Test
    void shouldThrowExceptionWhenStartDateInFuture() {

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);

        assertThrows(InvalidDateRangeException.class, () -> validator.validate(startDate, endDate, null));
    }

    @Test
    void shouldPassWhenValidDateRangeProvided() {

        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        assertDoesNotThrow(() -> validator.validate(startDate, endDate, null));
    }

    @Test
    void shouldPassWhenValidMonthsProvided() {

        assertDoesNotThrow(() -> validator.validate(null, null, 3));
    }

    @Test
    void shouldPassWhenAllOptionalParametersAreNull() {

        assertDoesNotThrow(() -> validator.validate(null, null, null));
    }

    @Test
    void shouldThrowExceptionWhenMonthsExceedsMaximumLimit() {

        InvalidMonthException exception =
                assertThrows(InvalidMonthException.class, () -> validator.validate(null, null, 37));

        assertEquals("Months cannot exceed 36", exception.getMessage());
    }

    @Test
    void shouldPassWhenMonthsEqualsMaximumLimit() {

        assertDoesNotThrow(() -> validator.validate(null, null, 36));
    }
}
