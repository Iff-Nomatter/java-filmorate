package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmGenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmLikeRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRatingRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    final String FILM_REQUEST = "SELECT * FROM FILM WHERE FILM_ID = ?";
    final String FILM_ALL_REQUEST = "select * from FILM";
    final String FILM_YEAR_FILTER_REQUEST = "SELECT * FROM film " +
            "WHERE EXTRACT(YEAR FROM release_date::date) = ?";
    final String FILM_GENRE_FILTER_REQUEST = "SELECT f.* FROM film AS f " +
            "JOIN film_genre AS fg ON f.film_id = fg.film_id " +
            "JOIN genre AS g ON fg.genre_id = g.genre_id " +
            "WHERE g.genre = ?";
    final String FILM_GENRE_YEAR_FILTER_REQUEST = "SELECT f.* FROM film AS f " +
            "JOIN film_genre AS fg ON f.film_id = fg.film_id " +
            "JOIN genre AS g ON fg.genre_id = g.genre_id " +
            "WHERE g.genre = ? AND EXTRACT(YEAR FROM release_date::date) = ?" +
            "LIMIT ?";
    final String FILM_LIKES_REQUEST = "SELECT * FROM FILM_LIKE WHERE FILM_ID = ?";
    final String FILM_RATING_REQUEST = "SELECT * FROM FILM_RATING WHERE RATING_ID = ?";
    final String FILM_GENRE_REQUEST = "select G.* from FILM_GENRE as FG inner join GENRE as G " +
                                      "ON FG.GENRE_ID = G.GENRE_ID where FG.FILM_ID = ?";
    final String FILM_INSERT = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, " +
            "DURATION, RATING) VALUES (?, ?, ?, ?, ?)";
    final String FILM_GENRE_INSERT = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
    final String FILM_LIKES_INSERT = "INSERT INTO FILM_LIKE (FILM_ID, USER_ID) VALUES (?, ?)";
    final String FILM_UPDATE = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
            "DURATION = ?, RATING = ? WHERE FILM_ID = ?";
    final String FILM_ADD_LIKE = "INSERT INTO FILM_LIKE SET FILM_ID = ?, USER_ID = ?";
    final String FILM_REMOVE_LIKE = "DELETE FROM FILM_LIKE WHERE FILM_ID = ? AND USER_ID = ?";

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(FILM_INSERT,
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId((Integer)keyHolder.getKey());
        if (film.getGenre() != null && !film.getGenre().isEmpty()) { //проверка, пока в тестах genre = null
            addFilmGenreData(film);
        }
        if (film.getLikeSet() != null && !film.getLikeSet().isEmpty()) {
            addFilmLikeData(film);
        }
    }

    private void addFilmGenreData(Film film) {
        List <FilmGenre> filmGenre = film.getGenre();
        for (FilmGenre genre : filmGenre) {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(FILM_GENRE_INSERT);
                ps.setInt(1, film.getId());
                ps.setInt(2, genre.getId());
                return ps;
            });
        }
    }

    private void addFilmLikeData(Film film) {
        Set<Integer> filmLikeSet = film.getLikeSet();
        for (Integer integer : filmLikeSet) {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(FILM_LIKES_INSERT);
                ps.setInt(1, film.getId());
                ps.setInt(2, integer);
                return ps;
            });
        }
    }

    @Override
    public void updateFilm(Film film) {
        getFilmById(film.getId()); //проверка на наличие записи в базе
        jdbcTemplate.update(FILM_UPDATE,
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
        jdbcTemplate.update(FILM_ADD_LIKE, film.getId(), userId);
    }

    @Override
    public void deleteLike(Film film, int userId) {
        film.getLikeSet().remove(userId);
        jdbcTemplate.update(FILM_REMOVE_LIKE, film.getId(), userId);
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> allFilms = jdbcTemplate.query(FILM_ALL_REQUEST, new FilmRowMapper());
        for (Film film : allFilms) {
            mapFilmProperties(film);
        }
        return allFilms;
    }

    @Override
    public List<Film> getPopular(Integer limit, String genre, Integer year) {
        List<Film> popularFilms;
        if (genre == null && year == null) {
            popularFilms = jdbcTemplate.query(FILM_ALL_REQUEST, new FilmRowMapper());
        } else if (genre == null) {
            popularFilms = jdbcTemplate.query(FILM_YEAR_FILTER_REQUEST, new FilmRowMapper(), year);
        } else if (year == null) {
            popularFilms = jdbcTemplate.query(FILM_GENRE_FILTER_REQUEST, new FilmRowMapper(), genre);
        } else {
            popularFilms = jdbcTemplate.query(FILM_GENRE_YEAR_FILTER_REQUEST, new FilmRowMapper(), genre, year);
        }
        return  popularFilms.stream()
                .map(this::mapFilmProperties)
                .collect(Collectors.toList());
    }

    @Override
    public Film getFilmById(int id) {
        Film film = jdbcTemplate.queryForObject(FILM_REQUEST,
                new FilmRowMapper(), id);
        if (film != null) {
            mapFilmProperties(film);
        }
        return film;
    }

    private Film mapFilmProperties(Film film) {
        FilmRating filmRating = jdbcTemplate.queryForObject(FILM_RATING_REQUEST,
                new FilmRatingRowMapper(), film.getMpa().getId());
        film.setMpa(filmRating);

        List<FilmGenre> filmGenre = jdbcTemplate.query(FILM_GENRE_REQUEST,
                new FilmGenreRowMapper(), film.getId());
        film.setGenre(filmGenre);

        List<Integer> filmLikeList = jdbcTemplate.query(FILM_LIKES_REQUEST, new FilmLikeRowMapper(), film.getId());
        Set<Integer> filmLikeSet = new HashSet<>(filmLikeList);
        film.setLikeSet(filmLikeSet);

        return film;
    }

}
