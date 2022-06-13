package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage storage;

    private final FilmStorage filmStorage;
    private final EventDbStorage eventDbStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage storage,
                       FilmStorage filmStorage, @Qualifier("eventDbStorage") EventDbStorage eventDbStorage) {
        this.storage = storage;
        this.filmStorage = filmStorage;
        this.eventDbStorage = eventDbStorage;
    }

    public void addUser(User user) {
        applyLoginToName(user);
        try {
            storage.addUser(user);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public void updateUser(User user) {
        applyLoginToName(user);
        try {
            storage.updateUser(user);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует пользователь c id: " + user.getId());
        }
    }

    public List<User> getAllUsers() {
        return storage.getAllUsers();
    }

    public User getUserById(int id) {
        try {
            return storage.getUserById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует пользователь c id: " + id);
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
        eventDbStorage.addEventToFeed(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public void deleteFromFriends(int userId, int friendId) {
        User user = getUserById(userId);
        User friendUser = getUserById(friendId);
        if (!user.getFriendSet().containsKey(friendId)) {
            throw new EntryNotFoundException("Пользователь с этим id не найден в списке друзей!");
        }
        storage.deleteFromFriends(user, friendUser);
        eventDbStorage.addEventToFeed(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    public List<User> getFriendsList(int userId) {
        List<User> friendList = new ArrayList<>();
        User user = storage.getUserById(userId);
        Map<Integer, FriendshipStatus> userFriendSet = user.getFriendSet();
        for (Integer integer : userFriendSet.keySet()) {
            friendList.add(storage.getUserById(integer));
        }
        return friendList;
    }

    public List<User> getCommonFriendsList(int userId, int otherId) {
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

    public List<Event> getUsersFeed(int id) {
        try {
            return eventDbStorage.getUsersFeed(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    public List<Film> getRecomendation(int id) {
        List<Film> filmsZeroUser;
        try {
            storage.getUserById(id);
            filmsZeroUser = filmStorage.getAllFilmsUser(id);
            if (filmsZeroUser.size() == 0) {
                throw new EntryNotFoundException("У пользователя с: " + id + " пока нет предпочтений");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        } catch (NullPointerException e) {
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
        List<User> allUserWithoutZeroUser = storage.getAllUsers()
                .stream()
                .filter((s) -> s.getId() != id)
                .collect(Collectors.toList());

        Map<Integer, Integer> userRatingRecomendation = new HashMap<>();
        List<Film> filmsUser;
        for (User user : allUserWithoutZeroUser) {
            int rating = 0;
            filmsUser = filmStorage.getAllFilmsUser(user.getId());
            for (Film filmZeroUser : filmsUser) {
                if (filmsZeroUser.contains(filmZeroUser)) {
                    rating++;
                }
            }
            userRatingRecomendation.put(user.getId(), rating);
        }
        int maxRatingUserId = userRatingRecomendation.entrySet()
                .stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();
        List<Film> filmsMaxRatingUser = filmStorage.getAllFilmsUser(maxRatingUserId);

        List<Film> recomendationFilm = new ArrayList<>();

        for (Film film : filmsMaxRatingUser) {
            if (!filmsZeroUser.contains(film)) {
                recomendationFilm.add(film);
            }
        }
    return recomendationFilm;
    }

    public void deleteUser(int userId) {
        storage.deleteUser(userId);
    }
}
