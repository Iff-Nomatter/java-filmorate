package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import javax.validation.constraints.Null;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement("INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, " +
                            "DURATION, RATING) VALUES (?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        try {
            film.setId((Integer)keyHolder.getKey());
        } catch (NullPointerException e){
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    @Override
    public void updateFilm(Film film) {
        Film filmToUpdate = getFilmById(film.getId()); //проверка на наличие записи в базе
        jdbcTemplate.update("UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                        "DURATION = ?, RATING = ? WHERE FILM_ID = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
    }

    @Override
    public void addLike(Film film, int userId) {
        film.getLikeSet().add(userId);
        jdbcTemplate.update("INSERT INTO FILM_LIKE SET FILM_ID = ?, USER_ID = ?",
                film.getId(), userId);
    }

    @Override
    public void deleteLike(Film film, int userId) {
        film.getLikeSet().remove(userId);
        jdbcTemplate.update("DELETE FROM FILM_LIKE WHERE FILM_ID = ? AND USER_ID = ?",
                film.getId(), userId);
    }

    @Override
    public List<Film> getAllFilms() {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from FILM");
        List<Film> allFilms = new ArrayList<>();
        while (filmRows.next()) {
            Film film = mapFilmFromDb(filmRows);
            mapFilmLikes(film);
            allFilms.add(film);
        }
        return allFilms;
    }

    @Override
    public Film getFilmById(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "select * from FILM where FILM_ID = ?", id);
        if (filmRows.next()) {
            Film film = mapFilmFromDb(filmRows);
            mapFilmLikes(film);
            log.info("Найден фильм: {} {}", film.getId(), film.getName());
            return film;
        } else {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        }
    }

    private Film mapFilmFromDb(SqlRowSet filmRow) {
        Film film = new Film();
        film.setId(filmRow.getInt("FILM_ID"));
        film.setName(filmRow.getString("NAME"));
        film.setDescription(filmRow.getString("DESCRIPTION"));
        java.sql.Date releaseDate = filmRow.getDate("RELEASE_DATE");
        film.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());
        film.setDuration(filmRow.getInt("DURATION"));

        int ratingId = filmRow.getInt("RATING");
        SqlRowSet filmRating = jdbcTemplate.queryForRowSet(
                "select * from FILM_RATING where RATING_ID = ?",
                ratingId);
        if (filmRating.next()) {
            String ratingName = filmRating.getString("MPA");
            FilmRating fetchedFilmRating = new FilmRating();
            fetchedFilmRating.setId(ratingId);
            fetchedFilmRating.setRating(ratingName);
            film.setMpa(fetchedFilmRating);
        }
        return film;
    }

    private void mapFilmLikes(Film film) {
        SqlRowSet filmLikes = jdbcTemplate.queryForRowSet(
                "select * from FILM_LIKE where FILM_ID = ?", film.getId());
        Set<Integer> likeSet = new HashSet<>();
        while (filmLikes.next()) {
            likeSet.add(filmLikes.getInt("USER_ID"));
        }
        film.setLikeSet(likeSet);
    }
}
