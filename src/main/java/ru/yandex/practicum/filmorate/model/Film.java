package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotations.DateValidation;
import ru.yandex.practicum.filmorate.annotations.PositiveDuration;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class Film extends IdHolder{
    @NotBlank
    private String name;
    @Length(max = 200)
    private String description;
    @DateValidation
    private LocalDate releaseDate;
    @PositiveDuration
    private Duration duration;
}
