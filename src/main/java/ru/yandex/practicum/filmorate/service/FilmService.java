package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage storage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
    }

    public void addFilm(Film film) {
        try {
            storage.addFilm(film);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public List<Film> getAllFilms() {
        return storage.getAllFilms();
    }

    public Film getFilmById(int id) {
        try {
            return storage.getFilmById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public void updateFilm(Film film) {
        try {
            storage.updateFilm(film);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + film.getId());
        }
    }

    public void addLike(int filmId, int userId) {
        userStorage.getUserById(userId);
        Film film = storage.getFilmById(filmId);
        if (film.getLikeSet().contains(userId)) {
            throw new EntryNotFoundException("Пользователь с id: " + userId + " уже ставил лайк этому фильму!");
        }
        storage.addLike(film, userId);
    }

    public void deleteLike(int filmId, int userId) {
        Film film = storage.getFilmById(filmId);
        Set<Integer> filmLikeSet = film.getLikeSet();
        if (!filmLikeSet.contains(userId)) {
            throw new EntryNotFoundException("Пользователь с id: " + userId + " не ставил лайк этому фильму!");
        }
        storage.deleteLike(film, userId);
    }
    
    public List<Film> getTopByLikes(Integer count, String genre, Integer year) {
        Comparator<Film> likeAmountComparator = Comparator.comparingInt(o -> o.getLikeSet().size());

        Predicate<Film> genreFilter;
        if (genre != null) {
            genreFilter = film -> film.getGenre().stream()
                    .anyMatch(filmGenre -> filmGenre.getName().equals(genre));
        } else {
            genreFilter = film -> true;
        }

        Predicate<Film> yearFilter;
        if (year != null) {
            yearFilter = film -> film.getReleaseDate().getYear() == year;
        } else {
            yearFilter = film -> true;
        }

        return storage.getAllFilms().stream()
                .filter(genreFilter)
                .filter(yearFilter)
                .sorted(likeAmountComparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
