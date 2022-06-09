package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.util.List;
import java.util.Optional;

public interface ReviewDao {
   FilmReview create(FilmReview review);

   FilmReview update(FilmReview review);

   Optional<FilmReview> get(Long id);

   List<FilmReview> getAll();

   FilmReview addLike(Long id, int userId, boolean isPositive);

   FilmReview updateLike(Long id, int userId, boolean isPositive);

   Optional<ReviewLike> getLike(Long id, int userId);

   FilmReview removeLike(Long id, int userId);

   List<FilmReview> getReviewByFilmId(int filmId);
}
