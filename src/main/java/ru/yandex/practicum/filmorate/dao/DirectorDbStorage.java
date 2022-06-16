package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmDirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Component("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    final String DIRECTOR_ALL_REQUEST = "SELECT * FROM FILM_DIRECTOR";
    final String DIRECTOR_REQUEST = "SELECT * FROM FILM_DIRECTOR WHERE DIRECTOR_ID = ?";
    final String DIRECTOR_DELETE = "DELETE FROM FILM_DIRECTOR WHERE DIRECTOR_ID = ?";
    final String DIRECTOR_UPDATE = "UPDATE FILM_DIRECTOR SET NAME = ? WHERE DIRECTOR_ID = ?";
    final String DIRECTOR_INSERT = "INSERT INTO FILM_DIRECTOR (NAME) VALUES (?)";
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addDirector(FilmDirector director) {
        jdbcTemplate.update(DIRECTOR_INSERT, director.getName());
    }

    @Override
    public void updateDirector(FilmDirector director) {
        jdbcTemplate.update(DIRECTOR_UPDATE, director.getName(), director.getId());
    }

    @Override
    public void deleteDirector(int id) {
        jdbcTemplate.update(DIRECTOR_DELETE, id);
    }

    @Override
    public FilmDirector getDirector(int id) {
        return jdbcTemplate.queryForObject(DIRECTOR_REQUEST, new FilmDirectorRowMapper(), id);
    }

    @Override
    public List<FilmDirector> getAllDirectors() {
        return jdbcTemplate.query(DIRECTOR_ALL_REQUEST, new FilmDirectorRowMapper());
    }
}
