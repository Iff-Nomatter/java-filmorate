package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Component
public class InMemoryUserStorage extends InMemoryStorage<User> implements UserStorage {

    @Override
    public void updateParameters(User user) {
        User userToUpdate = entries.get(user.getId());
        userToUpdate.setBirthday(user.getBirthday());
        userToUpdate.setLogin(user.getLogin());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
    }

    @Override
    public void doOnCreate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public void addUser(User user) {
        addEntry(user);
    }

    @Override
    public void updateUser(User user) {
        updateEntry(user);
    }

    @Override
    public void deleteUser(int id) {
        deleteEntry(id);
    }

    @Override
    public List<User> getAllUsers() {
        return getAll();
    }

    @Override
    public User getUserById(int id) {
        return getById(id);
    }
}
