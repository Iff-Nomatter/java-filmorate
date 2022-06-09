package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public Optional<FilmReview> getById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @PutMapping
    public FilmReview update(@RequestBody FilmReview filmReview) {
        return reviewService.update(filmReview);
    }

    @PostMapping
    public FilmReview create(@RequestBody FilmReview filmReview) {
        return reviewService.create(filmReview);
    }

    @GetMapping
    public List<FilmReview> getReviewByFilmId(@RequestParam(name = "film") int filmId) {
        return reviewService.getReviewByFilmId(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmReview addLike(@PathVariable Long id, @PathVariable int userId) {
        return reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public FilmReview addDislike(@PathVariable Long id, @PathVariable int userId) {
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmReview removeLike(@PathVariable Long id, @PathVariable int userId) {
        return reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public FilmReview removeDislike(@PathVariable Long id, @PathVariable int userId) {
        return reviewService.removeDislike(id, userId);
    }

}
