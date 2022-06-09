package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<FilmDirector> getAll() {
        List<FilmDirector> allDirectors = directorService.getAll();
        log.info("Режтссёров в базе: {}", allDirectors.size());
        return allDirectors;
    }

    @GetMapping("/{id}")
    public FilmDirector getDirector(@RequestParam Integer id) {
        log.info("Запрошен режиссёр id: " + id);
        return directorService.getDirector(id);
    }

    @PostMapping
    public ResponseEntity<FilmDirector> create(@Valid @RequestBody FilmDirector director) {
        directorService.addDirector(director);
        log.info("Новый фильм: " + director);
        return ResponseEntity.ok(director);
    }

    @PutMapping
    public ResponseEntity<FilmDirector> update(@Valid @RequestBody FilmDirector director) {
        directorService.updateDirector(director);
        log.info("Обновлен режиссёр: " + director);
        return ResponseEntity.ok(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestParam Integer id) {
        directorService.deleteDirector(id);
        log.info("Удалён режиссёр с id: " + id);
    }
}
