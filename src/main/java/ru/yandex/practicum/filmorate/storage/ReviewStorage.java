package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
   FilmReview create(FilmReview review);

   FilmReview update(FilmReview review);

   void remove(int id);

   Optional<FilmReview> get(int id);

   List<FilmReview> getAll(int count);

   FilmReview addLike(int id, int userId, boolean isPositive);

   FilmReview updateLike(int id, int userId, boolean isPositive);

   Optional<ReviewLike> getLike(int id, int userId);

   FilmReview removeLike(int id, int userId);

   List<FilmReview> getReviewByFilmId(int filmId, int count);
}
