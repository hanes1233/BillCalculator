package com.phonecompany.billing;

import com.phonecompany.billing.service.TelephoneBillCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

/**
 * Author: Pavel Herasymov
 * <br/>
 * Created: 12/17/25
 **/
@DisplayName("Bill calculator simple junit tests")
@SpringBootTest
class TelephoneBillCalculatorTest {

    @Autowired
    private TelephoneBillCalculator telephoneBillCalculator;

    @DisplayName("Calculate peak-tme total amount for three different destinations")
    @Test
    void when_calculateDifferentDestinations_then_calculated() {
        // given
        String phoneLog = """
        420774567454,13-01-2025 08:05:10,13-01-2025 08:10:20
        420776562353,13-01-2025 15:55:00,13-01-2025 16:05:30
        420774567453,14-01-2025 09:00:00,14-01-2025 09:07:45""";

        // when
        BigDecimal totalCost = telephoneBillCalculator.calculate(phoneLog);

        // then
        Assertions.assertNotNull(totalCost);
        Assertions.assertEquals(new BigDecimal("17.0"), totalCost);
    }

    @DisplayName("Exclude most frequently called number from cost")
    @Test
    void when_frequentlyCalled_then_excluded() {
        // given
        String phoneLog = """
        420774567454,13-01-2025 08:05:10,13-01-2025 08:10:20
        420776562353,13-01-2025 15:55:00,13-01-2025 16:05:30
        420774567454,14-01-2025 09:00:00,14-01-2025 09:07:45""";

        // when
        BigDecimal totalCost = telephoneBillCalculator.calculate(phoneLog);

        // then
        Assertions.assertNotNull(totalCost);
        Assertions.assertEquals(new BigDecimal("6.2"), totalCost); // number 420774567454 should be excluded from result
    }

    @DisplayName("Fail on blank log")
    @Test
    void when_phoneLogIsBlank_then_throwIllegalArgumentException() {
        // when && then
        Assertions.assertThrows(IllegalArgumentException.class, () -> telephoneBillCalculator.calculate(""));
    }

    @DisplayName("Fail on invalid input")
    @Test
    void when_phoneLogIsInvalidInput_then_throwIllegalArgumentException() {
        // given
        String phoneLog = "just random input";

        // when && then
        Assertions.assertThrows(IllegalArgumentException.class, () -> telephoneBillCalculator.calculate(phoneLog));
    }

    @DisplayName("Two most frequently called destinations: exclude higher numeric value")
    @Test
    void when_twoMostFrequentlyCalledDestinations_then_excludeHighestNumeric() {
        // given
        String phoneLog = """
        420774567454,13-01-2025 08:05:10,13-01-2025 08:10:20
        420776562353,13-01-2025 15:55:00,13-01-2025 16:05:30
        420774567454,14-01-2025 09:00:00,14-01-2025 09:07:45
        420774555555,11-01-2025 09:00:00,14-01-2025 12:07:45
        420774555555,12-01-2025 09:00:00,14-01-2025 12:07:45""";

        // when
        BigDecimal totalCost = telephoneBillCalculator.calculate(phoneLog);

        // then
        Assertions.assertNotNull(totalCost);
        Assertions.assertEquals(new BigDecimal("1529.4"), totalCost); // expecting 420776562353 to be excluded as highest
    }
}
