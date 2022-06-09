package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.FilmDirector;
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
    
    public List<Film> getTopByLikes(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }
        Comparator<Film> likeAmountComparator = Comparator.comparingInt(o -> o.getLikeSet().size());
        return storage.getAllFilms().stream()
                .sorted(likeAmountComparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getByDirector(int directorId, String sortBy) {
        Comparator<Film> comparator;
        if (sortBy.equals("year")) {
            comparator = Comparator.comparingInt(f -> f.getReleaseDate().getYear());
        } else if (sortBy.equals("likes")) {
            comparator = Comparator.comparingInt(f -> f.getLikeSet().size());
        } else {
            throw new IllegalArgumentException("Некорректное значение параметра sortBy: " + sortBy);
        }
        return storage.getByDirector(directorId).stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }

}
