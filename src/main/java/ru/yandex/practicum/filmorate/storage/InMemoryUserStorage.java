package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enumerations.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

@Component("inMemoryUserStorage")
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
    public void addToFriends(User user, User friend) {
        if (friend.getFriendSet().containsKey(user.getId())) {
            user.getFriendSet().put(friend.getId(), FriendshipStatus.APPROVED);
            friend.getFriendSet().put(user.getId(), FriendshipStatus.APPROVED);
        } else {
            user.getFriendSet().put(friend.getId(), FriendshipStatus.PENDING);
        }
    }

    @Override
    public void deleteFromFriends(User user,
                                  User friend) {
        Map<Integer, FriendshipStatus> userFriendSet = user.getFriendSet();
        Map<Integer, FriendshipStatus> friendUserFriendSet = friend.getFriendSet();

        if (!userFriendSet.containsKey(friend.getId())) {
            throw new EntryNotFoundException("Пользователь с этим id не найден в списке друзей!");
        }
        getUserById(user.getId()).getFriendSet().remove(friend.getId());
        if (friendUserFriendSet.containsKey(user.getId())) {
            friendUserFriendSet.put(user.getId(), FriendshipStatus.PENDING);
        }
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
