package com.phonecompany.billing.service;

import java.math.BigDecimal;

/**
 * Author: Pavel Herasymov
 * <br />
 * Created: 12/17/25
 **/
public interface TelephoneBillCalculator {

    /**
     * Calculate and return total cost of all records in {@code phoneLog} for specific customer
     * (customer determining is not specified by task, assume it's handled by other methods in application)
     * <p>Method's flow:</p>
     * <ol>
     *     <li>Accept and validate phoneLog</li>
     *     <ul>
     *         <li>If phoneLog is blank or null, throw {@link IllegalArgumentException} and finish processing immediately</li>
     *         <li>Otherwise continue</li>
     *     </ul>
     *     <li>
     *         Map {@code phoneLog} into list of {@link com.phonecompany.billing.model.CallRecord} objects, call information.
     *         Mapping includes validating phone number with {@link com.phonecompany.billing.utils.PhoneNumberConverter}
     *         (regex to starts with 420 and max 12-digits long), parsing and validating start and end dates and
     *         calculating call duration in seconds based on start and end call time)
     *     </li>
     *     <li>Calculates total amount(cost) with {@code doCalculate()} method, which, first excludes most frequently called
     *     number (decided to be > 1 call to make logical sense) and then calculates cost depending on call start time, accordingly
     *     to provided in task use cases</li>
     *     <li>All method processing is wrapped in {@code try-catch} block to log error message and throw exception.
     *     Could be refactored to silently finishing/skipping current csv log</li>
     * </ol>
     * @param phoneLog CSV input in string variable
     * @return total amount to pay for all records
     */
    BigDecimal calculate (String phoneLog);
}
