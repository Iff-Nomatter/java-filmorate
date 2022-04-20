package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.annotations.PositiveDuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Duration;

public class DurationValidator implements ConstraintValidator<PositiveDuration, Duration> {
    @Override
    public boolean isValid(Duration duration, ConstraintValidatorContext constraintValidatorContext) {
        return !duration.isNegative();
    }
}
