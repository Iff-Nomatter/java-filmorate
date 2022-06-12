package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.model.FilmReview;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public FilmReview create(FilmReview review) {
        return reviewStorage.create(review);
    }

    public FilmReview update(FilmReview review) {
        return reviewStorage.update(review);
    }

    public void remove(int id) {
        reviewStorage.remove(id);
    }

    public Optional<FilmReview> getById(int id) {
        return reviewStorage.get(id);
    }

    public FilmReview addLike(int id, int userId) {
        if (reviewStorage.getLike(id, userId).isEmpty()) {
            return reviewStorage.addLike(id, userId, true);
        } else {
            return reviewStorage.updateLike(id, userId, true);
        }
    }

    public FilmReview addDislike(int id, int userId) {
        if (reviewStorage.getLike(id, userId).isEmpty()) {
            return reviewStorage.addLike(id, userId, false);
        } else {
            return reviewStorage.updateLike(id, userId, false);
        }
    }

    public FilmReview removeLike(int id, int userId) {
        return reviewStorage.removeLike(id, userId);
    }

    public FilmReview removeDislike(int id, int userId) {
        return reviewStorage.removeLike(id, userId);
    }

    public List<FilmReview> getFilmReview(int filmId, int count) {
        if (filmId == 0) {
          return reviewStorage.getAll(count);
        } else {
          return reviewStorage.getReviewByFilmId(filmId, count);
        }
    }

    public List<FilmReview> getAll(int count) {
        return reviewStorage.getAll(count);
    }

}
