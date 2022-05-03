package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage storage, UserStorage userStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
    }

    public void addLike(int filmId, int userId) {
        try {
            userStorage.getUserById(userId);
        } catch (EntryNotFoundException exception) {
            throw new EntryNotFoundException("Не найден пользователь с id: " + userId);
        }
        storage.getFilmById(filmId).getLikeSet().add(userId);
    }

    public void deleteLike(int filmId, int userId) {
        Set<Integer> filmLikeSet = storage.getFilmById(filmId).getLikeSet();
        if (!filmLikeSet.contains(userId)) {
            throw new EntryNotFoundException("Пользователь с id: " + userId + " не ставил лайк этому фильму!");
        }
        filmLikeSet.remove(userId);
    }
    
    public List<Film> getTopByLikes(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }
        List<Film> allFilms = storage.getAllFilms();
        allFilms.sort((o1, o2) -> {
            int a = o1.getLikeSet().size();
            int b = o2.getLikeSet().size();
            return Integer.compare(b, a);
        });
        if (allFilms.size() < count) {
            count = allFilms.size();
        }
        return allFilms.subList(0, count);
    }
}
