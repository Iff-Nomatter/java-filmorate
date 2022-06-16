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

    final String REVIEW_CREATE = "INSERT INTO film_review(film_id, user_id, content, is_positive) " +
            "VALUES (?, ?, ?, ?)";
    final String REVIEW_REMOVE = "DELETE FROM film_review WHERE id = ?";
    final String REVIEW_REQUEST = "SELECT * FROM film_review WHERE id = ?";
    final String REVIEW_REQUEST_ALL = "SELECT * FROM film_review ORDER BY useful DESC LIMIT ?";
    final String REVIEW_ADD_LIKE = "INSERT INTO review_like(is_positive, film_review_id, user_id) " +
            "VALUES (?, ?, ?)";
    final String REVIEW_UPDATE_LIKE = "UPDATE review_like SET is_positive = ? " +
            "WHERE film_review_id = ? AND user_id = ?";
    final String REVIEW_GET_LIKE = "SELECT * FROM review_like WHERE film_review_id = ? " +
            "AND user_id = ?";
    final String REVIEW_REMOVE_LIKE = "DELETE FROM review_like WHERE film_review_id = ? " +
            "AND user_id = ?";
    final String REVIEW_REQUEST_BY_FILM_ID = "SELECT * FROM film_review WHERE film_id = ? " +
            "ORDER BY useful DESC LIMIT ?";
    final String REVIEW_SET_USEFUL = "UPDATE film_review SET useful = ? WHERE id = ?";
    final String REVIEW_USEFULNESS_RATING = "SELECT " +
            "SUM(CASE is_positive WHEN true THEN 1 ELSE 0 END) AS like_count, " +
            "SUM(CASE is_positive WHEN false THEN 1 ELSE 0 END) AS dislike_count " +
            "FROM review_like WHERE film_review_id = ?";

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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(REVIEW_CREATE, new String[]{"id"});
            stmt.setInt(1, review.getFilmId()); //film_id
            stmt.setInt(2, review.getUserId()); //user_id
            stmt.setString(3, review.getContent()); //content
            stmt.setBoolean(4, review.getIsPositive()); //is_positive
            return stmt;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        //ID сохраненой записи в БД
        review.setId(id);
        return review;
    }

    @Override
    public void remove(int id) {
        get(id);
        jdbcTemplate.update(REVIEW_REMOVE, id);
    }

    @Override
    public FilmReview update(FilmReview reviewToUpdate) {
        FilmReview filmReview = get(reviewToUpdate.getId());
        filmReview.setContent(reviewToUpdate.getContent());
        filmReview.setIsPositive(reviewToUpdate.getIsPositive());

        String REVIEW_UPDATE = "UPDATE film_review " +
                "SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(REVIEW_UPDATE, filmReview.getContent(),
                filmReview.getIsPositive(), filmReview.getId());

        return filmReview;
    }

    @Override
    public FilmReview get(int id) {
        FilmReview filmReview;
        filmReview = jdbcTemplate.queryForObject(REVIEW_REQUEST, new FilmReviewRowMapper(), id);
        return filmReview;
    }

    @Override
    public List<FilmReview> getAll(int count) {
        return jdbcTemplate.query(REVIEW_REQUEST_ALL, new FilmReviewRowMapper(), count);
    }

    @Override
    public FilmReview addLike(int id, int userId, boolean isPositive) {
        return operationWithLike(id, userId, isPositive, REVIEW_ADD_LIKE);
    }

    @Override
    public FilmReview updateLike(int id, int userId, boolean isPositive) {
        return operationWithLike(id, userId, isPositive, REVIEW_UPDATE_LIKE);
    }

    private FilmReview operationWithLike(int id, int userId, boolean isPositive, String sqlQuery) {
        FilmReview filmReview = get(id);
        userStorage.getUserById(userId);
        jdbcTemplate.update(sqlQuery, isPositive, id, userId);
        //Пересчитаем индекс полезности
        int useful = getUsefulByReviewId(id).orElse(0);
        //Записываем его в базу
        setUsefulInReview(useful, id);
        filmReview.setUseful(useful);
        return filmReview;
    }

    @Override
    public Optional<ReviewLike> getLike(int id, int userId) {
        try {
            ReviewLike reviewLike = jdbcTemplate.queryForObject(REVIEW_GET_LIKE, this::makeReviewLike, id, userId);
            return Optional.ofNullable(reviewLike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public FilmReview removeLike(int id, int userId) {
        getLike(id, userId);
        FilmReview filmReview = get(id);
        jdbcTemplate.update(REVIEW_REMOVE_LIKE, id, userId);

        //Пересчитаем индекс полезности
        int useful = getUsefulByReviewId(id).orElse(0);
        //Записываем его в базу
        setUsefulInReview(useful, id);

        filmReview.setUseful(useful);
        return filmReview;
    }

    @Override
    public List<FilmReview> getReviewByFilmId(int filmId, int count) {
        return jdbcTemplate.query(REVIEW_REQUEST_BY_FILM_ID, new FilmReviewRowMapper(), filmId, count);
    }

    private ReviewLike makeReviewLike(ResultSet rs, int rowNum) throws SQLException {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReviewId(rs.getInt("film_review_id"));
        reviewLike.setUserId(rs.getInt("user_id"));
        reviewLike.setPositive(rs.getBoolean("is_positive"));
        return reviewLike;
    }


    private void setUsefulInReview(int useful, int id) {
        jdbcTemplate.update(REVIEW_SET_USEFUL, useful, id);
    }

    // рейтинг полезности отзыва
    private Optional<Integer> getUsefulByReviewId(int id) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(REVIEW_USEFULNESS_RATING, id);
        if (sqlRowSet.next()) {
            int like = sqlRowSet.getInt("like_count");
            int dislike = sqlRowSet.getInt("dislike_count");
            return Optional.of(like - dislike);
        } else {
            return Optional.empty();
        }
    }
}
