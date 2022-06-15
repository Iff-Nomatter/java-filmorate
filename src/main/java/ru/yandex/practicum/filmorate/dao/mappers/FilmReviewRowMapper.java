package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FilmReview;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmReviewRowMapper implements RowMapper<FilmReview> {
    @Override
    public FilmReview mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmReview filmReview = new FilmReview();
        filmReview.setId(rs.getInt("id"));
        filmReview.setFilmId(rs.getInt("film_id"));
        filmReview.setUserId(rs.getInt("user_id"));
        filmReview.setContent(rs.getString("content"));
        filmReview.setIsPositive(rs.getBoolean("is_positive"));
        filmReview.setUseful(rs.getInt("useful"));
        return filmReview;
    }
}
