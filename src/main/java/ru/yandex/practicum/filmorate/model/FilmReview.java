package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FilmReview {
    private Long reviewId;
    private String content;
    @JsonProperty("isPositive")
    private boolean positive;
    private int userId;
    private int filmId;
    private int useful;
}
