package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotations.DateValidation;
import ru.yandex.practicum.filmorate.annotations.PositiveDuration;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class Film extends IdHolder{
    @NotBlank
    private String name;
    @Length(min = 1, max = 200)
    private String description;
    @DateValidation
    private LocalDate releaseDate;
    @PositiveDuration(message = "не может быть отрицательна или равна нулю")
    private Duration duration;
    private Set<Integer> likeSet = new HashSet<>();
}
