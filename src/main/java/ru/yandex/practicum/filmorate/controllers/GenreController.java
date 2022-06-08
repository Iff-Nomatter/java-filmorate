package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final FilmService filmService;

    @Autowired
    public GenreController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmGenre> getAllGenres() {
        List<FilmGenre> allGenres = new ArrayList<>(filmService.getAllGenres());
        log.info("Жанров в базе: {}", allGenres.size());
        return allGenres;
    }

    @GetMapping("/{genreId}")
    public FilmGenre getGenreById(@PathVariable Integer genreId) {
        log.info("Запрошен жанр id: " + genreId);
        return filmService.getGenreById(genreId);
    }
}
