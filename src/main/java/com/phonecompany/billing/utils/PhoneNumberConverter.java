package com.phonecompany.billing.utils;

import com.opencsv.bean.AbstractBeanField;

/**
 * Author: Pavel Herasymov
 * <br />
 * Created: 12/17/25
 **/
public class PhoneNumberConverter extends AbstractBeanField<String, String> {

    @Override
    protected String convert(String value) {
        // Validate if starts with 420 and has length of 12 digits exactly
        if (value == null || !value.matches("420\\d{9}")) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
        return value;
    }
}
