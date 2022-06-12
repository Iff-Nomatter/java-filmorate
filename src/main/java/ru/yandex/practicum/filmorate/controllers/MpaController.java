package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final FilmService filmService;

    @Autowired
    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmRating> getAllRatings() {
        List<FilmRating> allRatings = new ArrayList<>(filmService.getAllRatings());
        log.info("Рейтингов в базе: {}", allRatings.size());
        return allRatings;
    }

    @GetMapping("/{ratingId}")
    public FilmRating getRatingById(@PathVariable Integer ratingId) {
        log.info("Запрошен рейтинг id: " + ratingId);
        return filmService.getRatingById(ratingId);
    }
}
