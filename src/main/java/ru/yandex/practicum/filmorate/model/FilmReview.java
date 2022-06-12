package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FilmReview {
    private int reviewId;
    @NotBlank
    private String content;
    @NotNull
    @JsonProperty("isPositive")
    private boolean positive;
    @NotNull
    private int userId;
    @NotNull
    private int filmId;
    private int useful;
}
