package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;
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
    private final EventDbStorage eventDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage storage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("eventDbStorage") EventDbStorage eventDbStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
        this.eventDbStorage = eventDbStorage;
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
        eventDbStorage.addEventToFeed(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        Film film = storage.getFilmById(filmId);
        Set<Integer> filmLikeSet = film.getLikeSet();
        if (!filmLikeSet.contains(userId)) {
            throw new EntryNotFoundException("Пользователь с id: " + userId + " не ставил лайк этому фильму!");
        }
        storage.deleteLike(film, userId);
        eventDbStorage.addEventToFeed(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }
    
    public List<Film> getTopByLikes(Integer limit, String genre, Integer year) {
        Comparator<Film> likeAmountComparator = Comparator.comparingInt(o -> o.getLikeSet().size());
        return storage.getPopular(genre, year).stream()
                .sorted(likeAmountComparator.reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
