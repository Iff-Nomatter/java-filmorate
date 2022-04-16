package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<String, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getAll() {
        log.info("Фильмов в базе: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Film film) {
        try {
            verifyFilm(film);
        } catch (ValidationException exception) {
            log.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        films.put(film.getName(), film);
        log.info("Новый фильм: " + film);
        return ResponseEntity.ok("verified");
    }

    @PutMapping
    public ResponseEntity<String> update(@RequestBody Film film) {
        try {
            verifyFilm(film);
        } catch (ValidationException exception) {
            log.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        films.put(film.getName(), film);
        log.info("Обновленный фильм: " + film);
        return ResponseEntity.ok("verified");
    }

    public void verifyFilm(Film film) {
        LocalDate earliestPossible = LocalDate.of(1895, 12, 28);
        if (film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым!");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов!");
        }
        if (film.getReleaseDate().isBefore(earliestPossible)) {
            throw new ValidationException("Дата выхода не может быть ранее, чем " + earliestPossible);
        }
        if (film.getDuration().isNegative()) {
            throw new ValidationException("Длительность не может быть отрицательной!");
        }
    }
}
