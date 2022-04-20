package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

class FilmControllerTest {

    FilmController controller = new FilmController();
    static Film film;
    static Film updatedFilm;

    @BeforeEach
    void init() {
        film = new Film();
        film.setName("War of the Worlds");
        film.setDescription("Film about alien invasion");
        film.setReleaseDate(LocalDate.of(2005, 6, 29));
        film.setDuration(Duration.ofMinutes(110));

        updatedFilm = new Film();
        updatedFilm.setId(1);
        updatedFilm.setName("War of the Worlds");
        updatedFilm.setDescription("Film about alien invasion");
        updatedFilm.setReleaseDate(LocalDate.of(2005, 6, 29));
        updatedFilm.setDuration(Duration.ofMinutes(116));

        controller.create(film);
    }

    @Test
    void shouldReturnListOfFilms() {
        Assert.notEmpty(controller.getAll(), "Вернулся пустой список, так быть не должно!");
    }

    @Test
    void shouldCreateFilm() {
        film.setName("War of the Worlds with Tom Cruise");
        film.setId(0);
        Assertions.assertEquals(ResponseEntity.ok("verified"), controller.create(film));
    }

    @Test
    void shouldUpdateFilm() {
        controller.update(updatedFilm);
        List<Film> filmList = controller.getAll();
        Film checkFilm = null;
        for (Film film1 : filmList) {
            if (film1.equals(updatedFilm)) {
                checkFilm = film1;
            }
        }
        Assertions.assertNotNull(checkFilm);
    }
}