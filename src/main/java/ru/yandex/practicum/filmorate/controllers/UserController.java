package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController extends Controller<User> {

    @Override
    public void updateParameters(User user) {
        User userToUpdate = entries.get(user.getId());
        userToUpdate.setBirthday(user.getBirthday());
        userToUpdate.setLogin(user.getLogin());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
        log.info("Обновленный пользователь: " + user);
    }

    @Override
    public void doOnCreate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
