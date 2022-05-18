package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAll() {
        List<User> allUsers = new ArrayList<>(userService.getAllUsers());
        log.info("Пользователей в базе: {}", allUsers.size());
        return allUsers;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        log.info("Запрошен пользователь id: " + id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("Запрошен список друзей пользователя id: " + id);
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Пользователь id: " + id + " запросил список общих друзей\n" +
                "с пользователем id: " + otherId);
        return userService.getCommonFriendsList(id, otherId);
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        userService.addUser(user);
        log.info("Новый пользователь: " + user);
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        userService.updateUser(user);
        log.info("Обновлен пользователь: " + user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable int id, @PathVariable int friendId) {
        userService.addToFriends(id, friendId);
        log.info("Пользователь id: " + id + " добавил в друзья пользователя id: " + friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable int id, @PathVariable int friendId) {
        userService.deleteFromFriends(id, friendId);
        log.info("Пользователь id" + id + " удалил из друзей пользователя id" + friendId + " :(");
    }
}
