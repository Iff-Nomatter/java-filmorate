package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class FilmControllerTest {

    private static final String ADDRESS = "http://localhost:";
    private static final String ENDPOINT = "/films";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FilmController controller;

    static Film film;
    static Film updatedFilm;
    static Film badFilm;
    static User theOneWhoLikes;
    static URI url;

    @BeforeEach
    void init() {
        url = URI.create(ADDRESS + port + ENDPOINT);

        film = new Film();
        film.setName("War of the Worlds");
        film.setDescription("Film about alien invasion");
        film.setReleaseDate(LocalDate.of(2005, 6, 29));
        film.setDuration(Duration.ofMinutes(110));

        updatedFilm = new Film();
        updatedFilm.setId(1);
        updatedFilm.setName("War of the Worlds with Tom Cruise");
        updatedFilm.setDescription("Film about alien invasion");
        updatedFilm.setReleaseDate(LocalDate.of(2005, 6, 29));
        updatedFilm.setDuration(Duration.ofMinutes(116));

        badFilm = new Film();
        badFilm.setName("name");
        badFilm.setDescription("description");
        badFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        badFilm.setDuration(Duration.ofMinutes(30));

        theOneWhoLikes = new User();
        theOneWhoLikes.setEmail("mail@mail.ru");
        theOneWhoLikes.setLogin("nagibator");
        theOneWhoLikes.setName("Pyotr");
        theOneWhoLikes.setBirthday(LocalDate.of(1980, 10, 15));
    }

    @Test
    void shouldReturnListOfFilms() {
        restTemplate.postForObject(url, film, String.class);
        assertThat(this.restTemplate.getForObject(url, List.class)).isNotEmpty();
    }

    @Test
    void shouldCreateFilm() {
        ResponseEntity<String> response = restTemplate.postForEntity(url, film, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldUpdateFilm() {
        restTemplate.postForObject(url, film, String.class);
        HttpEntity<Film> updatedFilmRequest = new HttpEntity<>(updatedFilm);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT,
                updatedFilmRequest, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldPutLike() {
        restTemplate.postForObject(ADDRESS + port + "/users", theOneWhoLikes, String.class);
        restTemplate.postForEntity(url, film, String.class);
        ResponseEntity<String> response = restTemplate.exchange(url + "/1/like/1", HttpMethod.PUT,
                new HttpEntity<>(null), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldDeleteLike() {
        restTemplate.postForObject(ADDRESS + port + "/users", theOneWhoLikes, String.class);
        restTemplate.postForEntity(url, film, String.class);
        restTemplate.exchange(url + "/1/like/1", HttpMethod.PUT,
                new HttpEntity<>(null), String.class);
        ResponseEntity<String> response = restTemplate.exchange(url + "/1/like/1", HttpMethod.DELETE,
                new HttpEntity<>(null), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturnTopFilms() {
        restTemplate.postForObject(ADDRESS + port + "/users", theOneWhoLikes, String.class);
        restTemplate.postForEntity(url, film, String.class);
        restTemplate.exchange(url + "/1/like/1", HttpMethod.PUT,
                new HttpEntity<>(null), String.class);

        assertThat(this.restTemplate.getForObject(url + "/popular", List.class)).isNotEmpty();
    }

    @Test
    void shouldReturn500WhenIdPresentOnCreate() {
        badFilm.setId(15);
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn404WhenNoIdInDatabaseOnUpdate() {
        badFilm.setId(197);
        HttpEntity<Film> badUpdateRequest = new HttpEntity<>(badFilm);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT,
                badUpdateRequest, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn404WhenNoIdOnUpdate() {
        badFilm.setId(0);
        HttpEntity<Film> badUpdateRequest = new HttpEntity<>(badFilm);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT,
                badUpdateRequest, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenEmptyName() {
        badFilm.setName("");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenDescription1CharacterLong() {
        badFilm.setDescription("1");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenDescriptionIs200CharLong() {
        badFilm.setDescription("descriptiondescriptiondescriptiondescriptiondescriptiondescription"+
                "descriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                "descriptiondescriptiondescriptiondescripThisDescriptionIs200CharLong");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenDescriptionTooLong() {
        badFilm.setDescription("descriptiondescriptiondescriptiondescriptiondescriptiondescription"+
                "descriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                "descriptiondescriptiondescriptiondescriptThisDescriptionIs201CharLong");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenReleasedTooEarly() {
        badFilm.setReleaseDate(LocalDate.of(1721, 1, 1));
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenItIsFirstFilmInHistory() {
        badFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenDurationIsZero() {
        badFilm.setDuration(Duration.ofMinutes(0));
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenNegativeDuration() {
        badFilm.setDuration(Duration.ofMinutes(-30));
        ResponseEntity<String> response = restTemplate.postForEntity(url, badFilm, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}