package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.FilmReview;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewService {
    private final ReviewDao reviewDao;

    public ReviewService(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    public FilmReview create(FilmReview review) {
        return reviewDao.create(review);
    }

    public FilmReview update(FilmReview review) {
        return reviewDao.update(review);
    }

    public Optional<FilmReview> getById(Long id) {
        return reviewDao.get(id);
    }

    public FilmReview addLike(Long id, int userId) {
        if (reviewDao.getLike(id, userId).isEmpty()) {
            return reviewDao.addLike(id, userId, true);
        } else {
            return reviewDao.updateLike(id, userId, true);
        }
    }

    public FilmReview addDislike(Long id, int userId) {
        if (reviewDao.getLike(id, userId).isEmpty()) {
            return reviewDao.addLike(id, userId, false);
        } else {
            return reviewDao.updateLike(id, userId, false);
        }
    }

    public FilmReview removeLike(Long id, int userId) {
            return reviewDao.removeLike(id, userId);
    }

    public FilmReview removeDislike(Long id, int userId) {
        return reviewDao.removeLike(id, userId);
    }

    public List<FilmReview> getReviewByFilmId(int filmId) {
        return reviewDao.getReviewByFilmId(filmId);
    }

    public List<FilmReview> getAll() {
        return reviewDao.getAll();
    }

}
