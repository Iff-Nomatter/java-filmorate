package ru.yandex.practicum.filmorate.annotations;

import ru.yandex.practicum.filmorate.validators.DurationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = DurationValidator.class)
@Documented
public @interface PositiveDuration {
    String message() default "не может быть отрицательна";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}