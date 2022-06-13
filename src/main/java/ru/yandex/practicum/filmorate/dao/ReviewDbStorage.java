package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class ReviewDbStorage implements ReviewStorage {
    JdbcTemplate jdbcTemplate;
    UserStorage userStorage;
    FilmStorage filmStorage;

    public ReviewDbStorage(
            JdbcTemplate jdbcTemplate,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public FilmReview create(FilmReview review) {
        String sqlQuery = "INSERT INTO film_review(film_id, user_id, content, is_positive) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setInt(1, review.getFilmId()); //film_id
            stmt.setInt(2, review.getUserId()); //user_id
            stmt.setString(3, review.getContent()); //content
            stmt.setBoolean(4, review.isPositive()); //is_positive
            return stmt;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        //ID сохраненой записи в БД
        review.setReviewId(id);

        log.info("Добавлен {}", review);
        return review;
    }

    @Override
    public void remove(int id) {
        get(id);
        String sqlQuery = "DELETE FROM film_review WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public FilmReview update(FilmReview reviewToUpdate) {
        FilmReview filmReview = get(reviewToUpdate.getReviewId());
        filmReview.setFilmId(reviewToUpdate.getFilmId());
        filmReview.setUserId(reviewToUpdate.getUserId());
        filmReview.setContent(reviewToUpdate.getContent());
        filmReview.setPositive(reviewToUpdate.isPositive());

        String sqlQuery = "UPDATE film_review " +
                "SET film_id = ?, user_id = ?, content = ?, is_positive = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, filmReview.getFilmId(), filmReview.getUserId(), filmReview.getContent(),
                filmReview.isPositive(), filmReview.getReviewId());
        log.info("Обновлен {}", filmReview);
        return filmReview;
    }

    @Override
    public FilmReview get(int id) {
        try {
            FilmReview filmReview;
            String sqlQuery = "SELECT * " +
                    "FROM film_review " +
                    "WHERE id = ?";
            filmReview = jdbcTemplate.queryForObject(sqlQuery, new FilmReviewRowMapper(), id);
            return filmReview;
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException(String.format("FilmReview %s not found", id));
        }
    }

    @Override
    public List<FilmReview> getAll(int count) {
        String sqlQuery = "SELECT * FROM film_review ORDER BY useful DESC LIMIT ?";
        log.info("Получен список фильмов с лимитом {}", count);
        return jdbcTemplate.query(sqlQuery, new FilmReviewRowMapper(), count);
    }

    @Override
    public FilmReview addLike(int id, int userId, boolean isPositive) {
        FilmReview filmReview = get(id);
        userStorage.getUserById(userId);

        String sqlQuery = "INSERT INTO review_like(film_review_id, user_id, is_positive) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.update(sqlQuery, id, userId, isPositive);
        //Пересчитаем индекс полезности
        int useful = getUsefulByReviewId(id).orElse(0);
        //Записываем его в базу
        setUsefulInReview(useful, id);
        filmReview.setUseful(useful);

        log.info("Добавлен Like к отзыву {}", id);
        return filmReview;
    }

    @Override
    public FilmReview updateLike(int id, int userId, boolean isPositive) {
        FilmReview filmReview = get(id);
        userStorage.getUserById(userId);
        String sqlQuery = "UPDATE review_like SET is_positive = ? " +
                "WHERE film_review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, isPositive, id, userId);
        //Пересчитаем индекс полезности
        int useful = getUsefulByReviewId(id).orElse(0);
        //Записываем его в базу
        setUsefulInReview(useful, id);

        filmReview.setUseful(useful);
        log.info("Обновлен Like к отзыву {}", id);
        return filmReview;
    }

    @Override
    public Optional<ReviewLike> getLike(int id, int userId) {
        try {
            String sqlQuery = "SELECT * FROM review_like WHERE film_review_id = ? AND user_id = ?";
            ReviewLike reviewLike = jdbcTemplate.queryForObject(sqlQuery, this::makeReviewLike, id, userId);
            return Optional.ofNullable(reviewLike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public FilmReview removeLike(int id, int userId) {
        getLike(id, userId);
        FilmReview filmReview = get(id);
        String sqlQuery = "DELETE FROM review_like WHERE film_review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);

        //Пересчитаем индекс полезности
        int useful = getUsefulByReviewId(id).orElse(0);
        //Записываем его в базу
        setUsefulInReview(useful, id);

        filmReview.setUseful(useful);
        log.info("Удален Like к отзыву {}", id);
        return filmReview;
    }

    @Override
    public List<FilmReview> getReviewByFilmId(int filmId, int count) {
        String sqlQuery = "SELECT * FROM film_review WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        log.info("Получен список отзывов к фильму {} с лимитом {}", filmId, count);
        return jdbcTemplate.query(sqlQuery, new FilmReviewRowMapper(), filmId, count);
    }

    private ReviewLike makeReviewLike(ResultSet rs, int rowNum) throws SQLException {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReviewId(rs.getInt("film_review_id"));
        reviewLike.setUserId(rs.getInt("user_id"));
        reviewLike.setPositive(rs.getBoolean("is_positive"));
        return reviewLike;
    }


    private void setUsefulInReview(int useful, int id) {
        String sqlQuery = "UPDATE film_review SET useful = ? WHERE id = ?";
        jdbcTemplate.update(sqlQuery, useful, id);
    }

    // рейтинг полезности отзыва
    private Optional<Integer> getUsefulByReviewId(int id) {
        String sqlQuery = "SELECT " +
                "SUM(CASE is_positive WHEN true THEN 1 ELSE 0 END) AS like_count, " +
                "SUM(CASE is_positive WHEN false THEN 1 ELSE 0 END) AS dislike_count " +
                "FROM review_like " +
                "WHERE film_review_id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (sqlRowSet.next()) {
            int like = sqlRowSet.getInt("like_count");
            int dislike = sqlRowSet.getInt("dislike_count");
            return Optional.of(like - dislike);
        } else {
            return Optional.empty();
        }
    }
}
