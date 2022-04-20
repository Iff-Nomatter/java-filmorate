package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.IdHolder;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
public abstract class Controller<T extends IdHolder> {
    protected final Map<Integer, T> entries = new HashMap<>();
    protected int idCounter = 1;

    @GetMapping
    public List<T> getAll() {
        log.info("Записей в базе: {}", entries.size());
        return new ArrayList<>(entries.values());
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody T entry) {
        if (entry.getId() != 0) {
            throw new ValidationException("Для создания не нужен id!");
        }
        doOnCreate(entry);
        setId(entry);
        entries.put(entry.getId(), entry);
        log.info("Новая запись: " + entry);
        return ResponseEntity.ok("verified");
    }

    @PutMapping
    public void update(@Valid @RequestBody T entry) {
        if (entry.getId() == 0) {
            throw new ValidationException("Для обновления записи нужен id!");
        }
        if (!entries.containsKey(entry.getId())) {
            throw new ValidationException("Отсутствует запись для обновления!");
        }
        updateParameters(entry);
    }

    //обновляет параметры объекта из переданного объекта
    abstract public void updateParameters(T entry);

    //будет переопределен в User для установки в поле name содержимого поля login
    public void doOnCreate(T entry) {
    }

    //присваивает id к новому объекту перед записью в базу
    public void setId(T entry) {
        entry.setId(idCounter++);
    }
}
