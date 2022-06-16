package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    void addUser(User user);

    void updateUser(User user);

    void deleteUser(int userId);

    void addToFriends(User user, User friend);

    void deleteFromFriends(User user, User friend);

    List<User> getAllUsers();

    User getUserById(int id);
}
