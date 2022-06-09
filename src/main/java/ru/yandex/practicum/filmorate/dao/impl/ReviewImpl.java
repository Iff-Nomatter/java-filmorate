package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReviewImpl implements ReviewDao {
    JdbcTemplate jdbcTemplate;
    UserStorage userStorage;
    FilmStorage filmStorage;

    public ReviewImpl(
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

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        //ID сохраненой записи в БД
        review.setReviewId(id);

        log.info("Добавлен {}", review);
        return review;
    }

    @Override
    public FilmReview update(FilmReview reviewToUpdate) {
        FilmReview filmReview = get(reviewToUpdate.getReviewId())
                .orElseThrow(() -> new EntryNotFoundException(
                        String.format("FilmReview %s not found", reviewToUpdate.getReviewId())
                ));

        filmReview.setFilmId(reviewToUpdate.getFilmId());
        filmReview.setUserId(reviewToUpdate.getUserId());
        filmReview.setContent(reviewToUpdate.getContent());
        filmReview.setPositive(reviewToUpdate.isPositive());

        String sqlQuery = "UPDATE film_review " +
                "SET film_id = ?, user_id = ?, content = ?, is_positive = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, filmReview.getFilmId(), filmReview.getUserId(), filmReview.getContent(),
                filmReview.isPositive(), filmReview.getReviewId());
        return filmReview;
    }

    @Override
    public Optional<FilmReview> get(Long id) {
        try {
            FilmReview filmReview;
            String sqlQuery = "SELECT * " +
                    "FROM film_review " +
                    "WHERE id = ?";
            filmReview = jdbcTemplate.queryForObject(sqlQuery, this::makeFilmReview, id);
            return Optional.ofNullable(filmReview);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException(String.format("FilmReview %s not found", id));
        }
    }

    @Override
    public List<FilmReview> getAll() {
        String sqlQuery = "SELECT * FROM film_review";
        return jdbcTemplate.query(sqlQuery, this::makeFilmReview);
    }

    @Override
    public FilmReview addLike(Long id, int userId, boolean isPositive) {
        FilmReview filmReview = get(id).orElseThrow(() -> new EntryNotFoundException(
                String.format("FilmReview %s not found", id)
        ));
        userStorage.getUserById(userId);

        String sqlQuery = "INSERT INTO review_like(film_review_id, user_id, is_positive) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.update(sqlQuery, id, userId, isPositive);
        filmReview.setUseful(isPositive ? filmReview.getUseful() + 1 : filmReview.getUseful() - 1);
        log.info("Добавлен Like к отзыву {}", id);
        return filmReview;
    }

    @Override
    public FilmReview updateLike(Long id, int userId, boolean isPositive) {
        FilmReview filmReview = get(id).orElseThrow(() -> new EntryNotFoundException(
                String.format("FilmReview %s not found", id)
        ));
        userStorage.getUserById(userId);
        String sqlQuery = "UPDATE review_like SET is_positive = ? " +
                "WHERE film_review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, isPositive, id, userId);
        log.info("Обновлен Like к отзыву {}", id);
        filmReview.setUseful(isPositive ? filmReview.getUseful() + 1 : filmReview.getUseful() - 1);
        return filmReview;
    }

    @Override
    public Optional<ReviewLike> getLike(Long id, int userId) {
        try {
            String sqlQuery = "SELECT * FROM review_like WHERE film_review_id = ? AND user_id = ?";
            ReviewLike reviewLike = jdbcTemplate.queryForObject(sqlQuery, this::makeReviewLike, id, userId);
            return Optional.ofNullable(reviewLike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public FilmReview removeLike(Long id, int userId) {
        getLike(id, userId);
        FilmReview filmReview = get(id).orElseThrow(() -> new EntryNotFoundException(
                String.format("FilmReview %s not found", id)
        ));
        String sqlQuery = "DELETE FROM review_like WHERE film_review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);
        log.info("Удален Like к отзыву {}", id);
        filmReview.setUseful(filmReview.getUseful() - 1);
        return filmReview;
    }

    @Override
    public List<FilmReview> getReviewByFilmId(int filmId) {
        String sqlQuery = "SELECT * FROM film_review WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, this::makeFilmReview, filmId).stream()
                .sorted(Comparator.comparingInt(FilmReview::getUseful).reversed())
                .collect(Collectors.toList());
    }

    private ReviewLike makeReviewLike(ResultSet rs, int rowNum) throws SQLException {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReviewId(rs.getLong("film_review_id"));
        reviewLike.setUserId(rs.getInt("user_id"));
        reviewLike.setPositive(rs.getBoolean("is_positive"));
        return reviewLike;
    }

    private FilmReview makeFilmReview(ResultSet rs, int rowNum) throws SQLException {
        FilmReview filmReview = new FilmReview();
        filmReview.setReviewId(rs.getLong("id"));
        filmReview.setFilmId(rs.getInt("film_id"));
        filmReview.setUserId(rs.getInt("user_id"));
        filmReview.setContent(rs.getString("content"));
        filmReview.setPositive(rs.getBoolean("is_positive"));
        // рейтинг полезности
        int useFul = getUseful(filmReview.getReviewId()).orElse(0);
        filmReview.setUseful(useFul);
        return filmReview;
    }


    // рейтинг полезности отзыва
    private Optional<Integer> getUseful(Long id) {
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
