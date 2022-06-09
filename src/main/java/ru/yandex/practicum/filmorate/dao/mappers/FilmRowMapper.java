package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getInt("FILM_ID"));
        film.setName(rs.getString("NAME"));
        film.setDescription(rs.getString("DESCRIPTION"));
        java.sql.Date releaseDate = rs.getDate("RELEASE_DATE");
        film.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());
        film.setDuration(rs.getInt("DURATION"));
        FilmRating filmRating = new FilmRating();
        filmRating.setId(rs.getInt("RATING"));
        film.setMpa(filmRating);
        FilmDirector filmDirector = new FilmDirector();
        filmDirector.setId(rs.getInt("DIRECTOR_ID"));
        film.setDirector(filmDirector);

        return film;
    }
}
