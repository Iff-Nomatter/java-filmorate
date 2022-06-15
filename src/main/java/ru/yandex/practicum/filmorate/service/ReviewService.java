package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.model.FilmReview;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    private final EventDbStorage eventDbStorage;

    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("eventDbStorage") EventDbStorage eventDbStorage,
                         @Qualifier("filmDbStorage") FilmDbStorage filmDbStorage,
                         @Qualifier("userDbStorage") UserDbStorage userDbStorage) {
        this.reviewStorage = reviewStorage;
        this.eventDbStorage = eventDbStorage;
        this.userDbStorage = userDbStorage;
        this.filmDbStorage = filmDbStorage;
    }

    public FilmReview create(FilmReview review) {
        validateIncomingReview(review);
        try {
            eventDbStorage.addEventToFeed(review.getUserId(),
                    EventType.REVIEW,
                    Operation.ADD,
                    review.getFilmId());
            log.info("Добавлен обзор: {}", review);
            return reviewStorage.create(review);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }

    }

    public FilmReview update(FilmReview review) {
        validateIncomingReview(review);
        try {
            eventDbStorage.addEventToFeed(review.getUserId(),
                    EventType.REVIEW,
                    Operation.UPDATE,
                    review.getFilmId());
            log.info("Обновлен обзор id: {}", review.getId());
            return reviewStorage.update(review);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует обзор c id: " + review.getId());
        }
    }

    public void remove(int id) {
        FilmReview reviewForDelete = reviewStorage.get(id);
        eventDbStorage.addEventToFeed(reviewForDelete.getUserId(),
                EventType.REVIEW,
                Operation.REMOVE,
                reviewForDelete.getId());
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
        reviewStorage.get(id);
        if (reviewStorage.getLike(id, userId).isEmpty()) {
            log.info("Добавлен Like к отзыву {}", id);
            return reviewStorage.addLike(id, userId, true);
        } else {
            log.info("Обновлен Like к отзыву {}", id);
            return reviewStorage.updateLike(id, userId, true);
        }
    }

    public FilmReview addDislike(int id, int userId) {
        if (reviewStorage.getLike(id, userId).isEmpty()) {
            log.info("Добавлен Dislike к отзыву {}", id);
            return reviewStorage.addLike(id, userId, false);
        } else {
            log.info("Обновлен Dislike к отзыву {}", id);
            return reviewStorage.updateLike(id, userId, false);
        }
    }

    public FilmReview removeLike(int id, int userId) {
        log.info("Удален Like к отзыву {}", id);
        return reviewStorage.removeLike(id, userId);
    }

    public FilmReview removeDislike(int id, int userId) {
        log.info("Удален DisLike к отзыву {}", id);
        return reviewStorage.removeLike(id, userId);
    }

    public List<FilmReview> getFilmReview(int filmId, int count) {
        if (filmId == 0) {
            log.info("Получен список фильмов с лимитом {}", count);
            return reviewStorage.getAll(count);
        } else {
            log.info("Получен список отзывов к фильму {} с лимитом {}", filmId, count);
            return reviewStorage.getReviewByFilmId(filmId, count);
        }
    }

    public List<FilmReview> getAll(int count) {
        return reviewStorage.getAll(count);
    }

    private void validateIncomingReview(FilmReview review) {
        if (review.getUserId() == 0 || review.getFilmId() == 0) {
            throw new ValidationException("Не указано id пользователя");
        }
        try {
            filmDbStorage.getFilmById(review.getFilmId());
            userDbStorage.getUserById(review.getUserId());
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует пользователь c id: " + review.getUserId());
        }
    }
}
