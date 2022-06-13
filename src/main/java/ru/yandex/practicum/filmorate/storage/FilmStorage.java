package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.List;

public interface FilmStorage {

    void addFilm(Film film);

    void updateFilm(Film film);

    void deleteFilm(int filmId);

    void addLike(Film film, int userId);

    void deleteLike(Film film, int userId);

    List<Film> getAllFilms();

    List<FilmRating> getAllRatings();

    FilmRating getRatingById(int ratingId);

    List<FilmGenre> getAllGenres();

    FilmGenre getGenreById(int genreId);

    Film getFilmById(int id);

    List<Film> getPopular(String genre, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> search(String query, SearchMode mode);

    List<Film> getByDirector(int directorId);
}
