package com.asd.cointrack.validation;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link MaxCurrentYear}.
 */
public class MaxCurrentYearValidator implements ConstraintValidator<MaxCurrentYear, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int currentYear = Year.now().getValue();
        return value <= currentYear;
    }
}
