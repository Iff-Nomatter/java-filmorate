package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserStorage storage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.storage = storage;
    }

    public void addUser(User user) {
        applyLoginToName(user);
        try {
            storage.addUser(user);
        } catch (NullPointerException e){
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public void updateUser(User user) {
        applyLoginToName(user);
        try {
            storage.updateUser(user);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + user.getId());
        }
    }

    public List<User> getAllUsers() {
        return storage.getAllUsers();
    }

    public User getUserById(int id) {
        try {
            return storage.getUserById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public void addToFriends(int userId, int friendId) {
        User user = getUserById(userId);
        User addedUser = getUserById(friendId);
        if (user.getFriendSet().containsKey(addedUser.getId())) {
            throw new ValidationException("Этот пользователь уже в друзьях!");
        }
        storage.addToFriends(user, addedUser);
    }

    public void deleteFromFriends(int userId, int friendId) {
        User user = getUserById(userId);
        User friendUser = getUserById(friendId);
        if (!user.getFriendSet().containsKey(friendId)) {
            throw new EntryNotFoundException("Пользователь с этим id не найден в списке друзей!");
        }
        storage.deleteFromFriends(user, friendUser);
    }

    public List<User> getFriendsList (int userId) {
        List<User> friendList = new ArrayList<>();
        User user = storage.getUserById(userId);
        Map<Integer, FriendshipStatus> userFriendSet = user.getFriendSet();
        for (Integer integer : userFriendSet.keySet()) {
            friendList.add(storage.getUserById(integer));
        }
        return friendList;
    }

    public List<User> getCommonFriendsList (int userId, int otherId) {
        User user = storage.getUserById(userId);
        User otherUser = storage.getUserById(otherId);
        List<User> commonFriends = new ArrayList<>();
        Map<Integer, FriendshipStatus> userFriendSet = user.getFriendSet();
        Map<Integer, FriendshipStatus> otherUserFriendSet = otherUser.getFriendSet();
        for (Integer integer : userFriendSet.keySet()) {
            if (otherUserFriendSet.containsKey(integer)) {
                commonFriends.add(storage.getUserById(integer));
            }
        }
        return commonFriends;
    }

    private void applyLoginToName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
