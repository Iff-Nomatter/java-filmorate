package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotations.DateValidation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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
    @Min(value = 1, message = "не может быть отрицательна или равна нулю")
    private int duration;
    private Set<Integer> likeSet = new HashSet<>();
}
