package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.model.FilmReview;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    private final EventDbStorage eventDbStorage;

    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("eventDbStorage") EventDbStorage eventDbStorage) {
        this.reviewStorage = reviewStorage;
        this.eventDbStorage = eventDbStorage;
    }

    public FilmReview create(FilmReview review) {
        try {
            eventDbStorage.addEventToFeed(review.getUserId(),
                    EventType.REVIEW,
                    Operation.ADD,
                    review.getFilmId());
            return reviewStorage.create(review);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public FilmReview update(FilmReview review) {
        try {
            eventDbStorage.addEventToFeed(review.getUserId(),
                    EventType.REVIEW,
                    Operation.UPDATE,
                    review.getFilmId());
            return reviewStorage.update(review);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует обзор c id: " + review.getReviewId());
        }
    }

    public void remove(int id) {
        FilmReview reviewForDelete = reviewStorage.get(id);
        eventDbStorage.addEventToFeed(reviewForDelete.getUserId(),
                EventType.REVIEW,
                Operation.REMOVE,
                reviewForDelete.getReviewId());
        reviewStorage.remove(id);
    }

    public FilmReview getById(int id) {
        try {
            return reviewStorage.get(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует обзор c id: " + id);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
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
