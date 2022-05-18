package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAll() {
        List<Film> allFilms = new ArrayList<>(filmService.getAllFilms());
        log.info("Фильмов в базе: {}", allFilms.size());
        return allFilms;
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.info("Запрошен фильм id: " + id);
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getTopByLikes(@RequestParam(required = false) Integer count) {
        List<Film> topFilmsByLikes = new ArrayList<>(filmService.getTopByLikes(count));
        log.info("Запрошен список из " + count + " лучших фильмов по лайкам.");
        return topFilmsByLikes;
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        filmService.addFilm(film);
        log.info("Новый фильм: " + film);
        return ResponseEntity.ok(film);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        filmService.updateFilm(film);
        log.info("Обновлен фильм: " + film);
        return ResponseEntity.ok(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
        log.info("Пользователь id: " + userId + " поставил лайк фильму id: " + id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        filmService.deleteLike(id, userId);
        log.info("Пользователь id: " + userId + " удалил лайк фильму id: " + id);
    }
}
