package com.phonecompany.billing.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.phonecompany.billing.model.CallRecord;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Pavel Herasymov
 * <br />
 * Created: 12/17/25
 **/
@Slf4j
@Service
public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final BigDecimal PEAK_RATE = new BigDecimal("1.0");
    private static final BigDecimal OFF_PEAK_RATE = new BigDecimal("0.5");
    private static final BigDecimal EXTRA_MINUTE_RATE = new BigDecimal("0.2");
    private static final long FIRST_MINUTES = 5;


    @Override
    public BigDecimal calculate(String phoneLog) {
        if (StringUtils.isBlank(phoneLog)) {
            throw new IllegalArgumentException("Phone log is empty");
        }

        try {
            List<CallRecord> callRecords = this.getCallRecords(phoneLog);
            return this.doCalculate(callRecords);
        } catch (Exception e) {
            log.error("Parsing or calculating call records failed", e);
            throw new IllegalArgumentException("Parsing or calculating call records failed: %s".formatted(e.getMessage()));
        }
    }

    private List<CallRecord> getCallRecords(String phoneLog) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        List<CallRecord> callRecordList = new CsvToBeanBuilder<CallRecord>(new StringReader(phoneLog))
                .withType(CallRecord.class)
                .build()
                .parse();

        callRecordList.forEach(callRecord -> {
            String destination = callRecord.getDestination();
            if (destination != null) {
                callRecord.setDestination(destination.trim());
            }
            callRecord.parseDates(dtf);
            callRecord.calculateDurationInSeconds();
        });
        return callRecordList;
    }

    private BigDecimal doCalculate(List<CallRecord> callRecords) {
        List<CallRecord> formattedRecordList = this.excludeMostFrequentDestination(callRecords);
        return formattedRecordList.stream()
                .map(this::getRecordAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getRecordAmount(CallRecord callRecord) {
        LocalDateTime callStart = callRecord.getStartTime();
        Long duration = callRecord.getDuration();
        LocalTime eightAM = LocalTime.of(8, 0);
        LocalTime fourPM = LocalTime.of(16, 0);
        LocalTime callTime = callStart.toLocalTime();

        boolean isPeak = !callTime.isBefore(eightAM) && callTime.isBefore(fourPM);

        if (isPeak) {
            return this.calculateRecordCost(duration, PEAK_RATE);
        }

        return this.calculateRecordCost(duration, OFF_PEAK_RATE);
    }

    private List<CallRecord> excludeMostFrequentDestination(List<CallRecord> callRecords) {
        // Count frequency of each destination
        Map<String, Long> frequencyMap = callRecords.stream()
                .collect(Collectors.groupingBy(CallRecord::getDestination, Collectors.counting()));

        // Find the highest frequency
        long maxFrequency = frequencyMap.values().stream().max(Long::compareTo).orElse(0L);

        // Nothing to exclude, just return all records back
        if (maxFrequency <= 1) {
            return callRecords;
        }

        // Find all numbers(destination) with the highest frequency
        List<String> mostFrequentNumbers = frequencyMap.entrySet().stream()
                .filter(e -> e.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .toList();

        // If multiple, pick the one with the highest numeric value
        String freeNumber = mostFrequentNumbers.stream()
                .map(BigDecimal::new)
                .max(BigDecimal::compareTo)
                .map(BigDecimal::toPlainString)
                .orElse("");

        // Return list excluding all calls to that number
        return callRecords.stream()
                .filter(c -> !c.getDestination().equals(freeNumber))
                .toList();
    }

    private BigDecimal calculateRecordCost(Long durationInSeconds, BigDecimal oneMinuteCost) {
        if (durationInSeconds == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
        // rounding trick
        long durationInMinutes = (durationInSeconds + 59) / 60;

        BigDecimal cost;

        if (durationInMinutes <= FIRST_MINUTES) {
            cost = oneMinuteCost.multiply(BigDecimal.valueOf(durationInMinutes));
        } else {
            BigDecimal firstFiveCost = oneMinuteCost.multiply(BigDecimal.valueOf(FIRST_MINUTES));
            long extraMinutes = durationInMinutes - FIRST_MINUTES;
            BigDecimal extraCost = BigDecimal.valueOf(extraMinutes).multiply(EXTRA_MINUTE_RATE);

            cost = firstFiveCost.add(extraCost);
        }

        return cost;
    }
}
