package ru.yandex.practicum.filmorate.annotations;

import ru.yandex.practicum.filmorate.validators.DateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = DateValidator.class)
@Documented
public @interface DateValidation {
    String message() default "не может быть раньше 27.12.1985";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
