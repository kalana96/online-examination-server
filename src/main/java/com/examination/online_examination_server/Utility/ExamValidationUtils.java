package com.examination.online_examination_server.Utility;

import com.examination.online_examination_server.exception.exam.ExamValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
public class ExamValidationUtils {
    public static void validateTimeFormat(String timeString, String fieldName) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new ExamValidationException(fieldName + " is required");
        }

        try {
            LocalTime.parse(timeString);
        } catch (DateTimeParseException e) {
            throw new ExamValidationException("Invalid " + fieldName.toLowerCase() + " format. Expected format: HH:mm", e);
        }
    }

    public static void validateDateFormat(LocalDate date, String fieldName) {
        if (date == null) {
            throw new ExamValidationException(fieldName + " is required");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new ExamValidationException(fieldName + " cannot be in the past");
        }
    }

    public static void validatePositiveNumber(Number number, String fieldName) {
        if (number == null || number.doubleValue() <= 0) {
            throw new ExamValidationException(fieldName + " must be a positive number");
        }
    }

    public static void validateStringNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ExamValidationException(fieldName + " is required");
        }
    }

    public static void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ExamValidationException("Valid " + fieldName + " is required");
        }
    }
}
