package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    void addFilm(Film film);

    void updateFilm(Film film);

    void addLike(Film film, int userId);

    void deleteLike(Film film, int userId);

    List<Film> getAllFilms();

    Film getFilmById(int id);

    List<Film> getPopular(String genre, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);
}
