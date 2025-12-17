package com.phonecompany.billing.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.phonecompany.billing.utils.PhoneNumberConverter;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Author: Pavel Herasymov
 * <br />
 * Created: 12/17/25
 **/
@Getter
@Setter
public class CallRecord {

    @CsvBindByPosition(position = 0)
    @CsvCustomBindByPosition(position = 0, converter = PhoneNumberConverter.class)
    private String destination;

    @CsvBindByPosition(position = 1)
    private String startStr;

    @CsvBindByPosition(position = 2)
    private String endStr;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;

    public void parseDates(DateTimeFormatter dtf) {
        startTime = LocalDateTime.parse(startStr, dtf);
        endTime = LocalDateTime.parse(endStr, dtf);
        if (endTime.isBefore(startTime)) {
            throw new IllegalStateException("End time cannot be before start time");
        }
    }

    public void calculateDurationInSeconds() {
        this.duration = Duration.between(startTime, endTime).toSeconds();
    }
}
