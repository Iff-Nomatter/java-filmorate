package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.SearchMode;

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
            "WHERE g.genre = ? AND EXTRACT(YEAR FROM release_date::date) = ?";
    final String FILM_LIKES_REQUEST = "SELECT * FROM FILM_LIKE WHERE FILM_ID = ?";
    final String FILM_RATING_REQUEST = "SELECT * FROM FILM_RATING WHERE RATING_ID = ?";
    final String FILM_GENRE_REQUEST = "select G.* from FILM_GENRE as FG inner join GENRE as G " +
            "ON FG.GENRE_ID = G.GENRE_ID where FG.FILM_ID = ?";
    final String FILM_INSERT = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, " +
            "DURATION, RATING, DIRECTOR_ID) VALUES (?, ?, ?, ?, ?, ?)";
    final String FILM_GENRE_INSERT = "MERGE INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
    final String FILM_GENRE_DELETE = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";
    final String FILM_LIKES_INSERT = "INSERT INTO FILM_LIKE (FILM_ID, USER_ID) VALUES (?, ?)";
    final String FILM_UPDATE = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
            "DURATION = ?, RATING = ?, DIRECTOR_ID = ? WHERE FILM_ID = ?";
    final String FILM_ADD_LIKE = "INSERT INTO FILM_LIKE SET FILM_ID = ?, USER_ID = ?";
    final String FILM_REMOVE_LIKE = "DELETE FROM FILM_LIKE WHERE FILM_ID = ? AND USER_ID = ?";
    final String FILM_REMOVE = "DELETE FROM FILM WHERE FILM_ID = ?";
    final String RATING_ALL_REQUEST = "SELECT * FROM FILM_RATING";
    final String RATING_REQUEST = "SELECT * FROM FILM_RATING WHERE RATING_ID = ?";
    final String GENRE_ALL_REQUEST = "SELECT * FROM GENRE";
    final String GENRE_REQUEST = "SELECT * FROM GENRE WHERE GENRE_ID = ?";

    final String GET_COMMON_FILMS_REQUEST = "SELECT * FROM FILM WHERE FILM_ID IN " +
            "(SELECT FILM_ID FROM FILM_LIKE WHERE FILM_ID IN " +
            "(SELECT FILM.FILM_ID FROM FILM WHERE FILM_ID IN (SELECT a.FILM_ID FROM " +
            "(SELECT * FROM FILM_LIKE WHERE USER_ID = ?) as a " +
            "INNER JOIN (SELECT * FROM FILM_LIKE WHERE USER_ID = ?) as b on a.FILM_ID = b.FILM_ID))" +
            " GROUP BY FILM_ID ORDER BY COUNT(FILM_ID) desc)";

    final String FILM_SEARCH_BY_NAME = "SELECT * FROM FILM WHERE LOWER(name) LIKE LOWER(?)";
    final String FILM_SEARCH_BY_DIRECTOR = "SELECT f.* FROM FILM AS F " +
            "LEFT JOIN FILM_DIRECTOR AS fd ON f.DIRECTOR_ID = fd.DIRECTOR_ID " +
            "WHERE LOWER(fd.NAME) LIKE LOWER(?)";

    final String FILM_SEARCH_BY_NAME_OR_DIRECTOR = "SELECT f.* FROM FILM AS F " +
            "LEFT JOIN FILM_DIRECTOR AS fd ON f.DIRECTOR_ID = fd.DIRECTOR_ID " +
            "WHERE LOWER(fd.NAME) LIKE LOWER(?) OR LOWER(f.NAME) LIKE LOWER(?)";

    final String FILM_BY_DIRECTOR_REQUEST = "SELECT * FROM FILM WHERE DIRECTOR_ID = ?";
    final String FILM_DIRECTOR_REQUEST = "SELECT * FROM FILM_DIRECTOR " +
            "WHERE DIRECTOR_ID = (SELECT DIRECTOR_ID FROM FILM WHERE FILM_ID = ?)";

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
            ps.setInt(6, film.getDirector().getId());
            return ps;
        }, keyHolder);
        film.setId((Integer) keyHolder.getKey());
        addFilmGenreData(film);
        if (film.getLikeSet() != null && !film.getLikeSet().isEmpty()) {
            addFilmLikeData(film);
        }
    }

    private void addFilmGenreData(Film film) {
        jdbcTemplate.update(FILM_GENRE_DELETE, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        LinkedHashSet<FilmGenre> filmGenre = film.getGenres();
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
                film.getDirector().getId(),
                film.getId());
        addFilmGenreData(film);
    }

    @Override
    public void deleteFilm(int filmId) {
        jdbcTemplate.update(FILM_REMOVE, filmId);
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
    public List<Film> getPopular(String genre, Integer year) {
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
    public List<FilmRating> getAllRatings() {
        return jdbcTemplate.query(RATING_ALL_REQUEST, new FilmRatingRowMapper());
    }

    @Override
    public FilmRating getRatingById(int ratingId) {
        return jdbcTemplate.queryForObject(RATING_REQUEST,
                new FilmRatingRowMapper(), ratingId);
    }

    @Override
    public List<FilmGenre> getAllGenres() {
        return jdbcTemplate.query(GENRE_ALL_REQUEST, new FilmGenreRowMapper());
    }

    @Override
    public FilmGenre getGenreById(int genreId) {
        return jdbcTemplate.queryForObject(GENRE_REQUEST,
                new FilmGenreRowMapper(), genreId);
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


    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> commonFilms = jdbcTemplate.query(GET_COMMON_FILMS_REQUEST, new FilmRowMapper(), userId, friendId);
        for (Film film : commonFilms) {
            mapFilmProperties(film);
        }
        return commonFilms;
    }

    @Override
    public List<Film> getByDirector(int directorId) {
        return jdbcTemplate.query(FILM_BY_DIRECTOR_REQUEST, new FilmRowMapper(), directorId).stream()
                .map(this::mapFilmProperties)
                .collect(Collectors.toList());
    }

    private Film mapFilmProperties(Film film) {
        FilmRating filmRating = jdbcTemplate.queryForObject(FILM_RATING_REQUEST,
                new FilmRatingRowMapper(), film.getMpa().getId());
        film.setMpa(filmRating);

        LinkedHashSet<FilmGenre> filmGenre = new LinkedHashSet<>(jdbcTemplate.query(FILM_GENRE_REQUEST,
                new FilmGenreRowMapper(), film.getId()));
        if (filmGenre.isEmpty()) {
            film.setGenres(null);
        } else {
            film.setGenres(filmGenre);
        }
        Set<Integer> filmLikeSet = new HashSet<>(jdbcTemplate.query(FILM_LIKES_REQUEST,
                new FilmLikeRowMapper(), film.getId()));
        film.setLikeSet(filmLikeSet);

        FilmDirector filmDirector = jdbcTemplate.queryForObject(FILM_DIRECTOR_REQUEST,
                new FilmDirectorRowMapper(), film.getId());
        film.setDirector(filmDirector);
        return film;
    }

    public List<Film> search(String query, SearchMode mode) {
        String term = "%" + query + "%";
        List<Film> allFilms;
        switch (mode) {
            case SEARCH_BY_TITLE:
                allFilms = jdbcTemplate.query(FILM_SEARCH_BY_NAME, new FilmRowMapper(), term);
                break;
            case SEARCH_BY_DIRECTOR:
                allFilms = jdbcTemplate.query(FILM_SEARCH_BY_DIRECTOR, new FilmRowMapper(), term);
                break;
            case SEARCH_BY_TITLE_OR_DIRECTOR:
                allFilms = jdbcTemplate.query(FILM_SEARCH_BY_NAME_OR_DIRECTOR, new FilmRowMapper(), term, term);
                break;
            default:
                throw new IllegalArgumentException("Unknown search mode");
        }

        for (Film film : allFilms) {
            mapFilmProperties(film);
        }
        return allFilms;
    }
}
