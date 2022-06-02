package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class FilmRating {
    @Positive
    @NotNull
    private int id;
    private String rating;
}
