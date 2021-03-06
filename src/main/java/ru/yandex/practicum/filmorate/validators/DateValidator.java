package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.annotations.DateValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<DateValidation, LocalDate> {
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext context) {
        return localDate.isAfter(LocalDate.of(1895, 12, 27));
        //проверяется дата 27.12.1895, чтобы фильм мог выйти и в граничную дату
    }
}
