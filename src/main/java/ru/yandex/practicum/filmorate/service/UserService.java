package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public void addUser(User user) {
        storage.addUser(user);
    }

    public void updateUser(User user) {
        storage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return storage.getAllUsers();
    }

    public User getUserById(int id) {
        return storage.getUserById(id);
    }

    public void addToFriends(int userId, int friendId) {
        User user = storage.getUserById(userId);
        User addedUser = storage.getUserById(friendId);
        user.getFriendSet().add(addedUser.getId());
        addedUser.getFriendSet().add(user.getId());
    }

    public void deleteFromFriends(int userId, int friendId) {
        User user = storage.getUserById(userId);
        User friendUser = storage.getUserById(friendId);
        Set<Integer> userFriendSet = user.getFriendSet();
        Set<Integer> friendUserFriendSet = friendUser.getFriendSet();
        if (!userFriendSet.contains(friendId) || !friendUserFriendSet.contains(userId)) {
            throw new EntryNotFoundException("Пользователь с этим id не найден в списке друзей!");
        }
        storage.getUserById(userId).getFriendSet().remove(friendId);
        storage.getUserById(friendId).getFriendSet().remove(userId);
    }

    public List<User> getFriendsList (int userId) {
        List<User> friendList = new ArrayList<>();
        User user = storage.getUserById(userId);
        Set<Integer> userFriendSet = user.getFriendSet();
        for (Integer integer : userFriendSet) {
            friendList.add(storage.getUserById(integer));
        }
        return friendList;
    }

    public List<User> getCommonFriendsList (int userId, int otherId) {
        User user = storage.getUserById(userId);
        User otherUser = storage.getUserById(otherId);
        List<User> commonFriends = new ArrayList<>();
        Set<Integer> userFriendSet = user.getFriendSet();
        Set<Integer> otherUserFriendSet = otherUser.getFriendSet();
        for (Integer integer : userFriendSet) {
            if (otherUserFriendSet.contains(integer)) {
                commonFriends.add(storage.getUserById(integer));
            }
        }
        return commonFriends;
    }
}
