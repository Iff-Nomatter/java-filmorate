package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @GetMapping("/users")
    public List<User> getAll() {
        log.info("Пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @PostMapping("/users")
    public ResponseEntity<String> create(@RequestBody User user) {
        try {
            if (user.getId() != 0) {
                throw new ValidationException("Для создания не нужен id!");
            }
            verifyUser(user);
        } catch (ValidationException exception) {
            log.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        setId(user);
        users.put(user.getId(), user);
        log.info("Новый пользователь: " + user);
        return ResponseEntity.ok("verified");
    }

    @PutMapping("/users")
    public ResponseEntity<String> update(@RequestBody User user) {
        try {
            if (user.getId() == 0) {
                throw new ValidationException("Для обновления пользователя нужен id!");
            }
            if (!users.containsKey(user.getId())) {
                throw new ValidationException("Отсутствует пользователь для обновления!");
            }
            verifyUser(user);
        } catch (ValidationException exception) {
            log.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User userToUpdate = users.get(user.getId());
        userToUpdate.setBirthday(user.getBirthday());
        userToUpdate.setLogin(user.getLogin());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
        log.info("Обновленный пользователь: " + user);
        return ResponseEntity.ok("verified");
    }

    public void verifyUser(User user) {
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Неверный формат e-mail!");
        }
        if (user.getLogin().isBlank()) {
            throw new ValidationException("Логин не должен быть пустым!");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("В логине не должно быть пробелов!");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void setId(User user) {
        user.setId(idCounter++);
    }
}
