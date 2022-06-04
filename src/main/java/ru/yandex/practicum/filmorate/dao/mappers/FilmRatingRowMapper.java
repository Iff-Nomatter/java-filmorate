package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRatingRowMapper implements RowMapper<FilmRating> {
    @Override
    public FilmRating mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmRating filmRating = new FilmRating();
        filmRating.setId(rs.getInt("RATING_ID"));
        filmRating.setName(rs.getString("MPA"));
        return filmRating;
    }
}
