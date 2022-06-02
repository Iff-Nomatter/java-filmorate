package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addUser(User user) {
        applyLoginToName(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement("INSERT INTO USERS (NAME, LOGIN, BIRTHDAY, EMAIL) " +
                            "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2,  user.getLogin());
            ps.setDate(3, java.sql.Date.valueOf(user.getBirthday()));
            ps.setString(4, user.getEmail());
            return ps;
        }, keyHolder);

        Integer userId = (Integer)keyHolder.getKey();
        if (!user.getFriendSet().isEmpty()) {
            Map<Integer, FriendshipStatus> friendSet = user.getFriendSet();
            for (Map.Entry<Integer, FriendshipStatus> entry : friendSet.entrySet()) {
                jdbcTemplate.update("INSERT INTO USER_FRIEND VALUES (?, ?, ?)",
                        userId, entry.getKey(), entry.getValue());
            }
        }
        try {
            user.setId(userId);
        } catch (NullPointerException e){
            throw new EntryNotFoundException("Что-то пошло не так в базе данных.");
        }
    }

    @Override
    public void updateUser(User user) {
        applyLoginToName(user);
        User userToUpdate = getUserById(user.getId()); //проверка на наличие записи в базе
        jdbcTemplate.update("UPDATE USERS SET NAME = ?, LOGIN = ?, BIRTHDAY = ?, EMAIL = ? " +
                        "WHERE USER_ID = ?",
                user.getName(),
                user.getLogin(),
                user.getBirthday(),
                user.getEmail(),
                user.getId());
    }

    @Override
    public void addToFriends(User user, User friend) {
        if (user.getFriendSet().containsKey(friend.getId())) {
            throw new ValidationException("Этот пользователь уже в друзьях!");
        }
        if (friend.getFriendSet().containsKey(user.getId())) {
            jdbcTemplate.update("UPDATE USER_FRIEND SET FRIENDSHIP_STATUS = ? WHERE " +
                            "USER_ID = ? AND FRIEND_ID = ? OR USER_ID = ? AND FRIEND_ID = ?",
                    FriendshipStatus.APPROVED.toString(),
                    user.getId(),
                    friend.getId(),
                    friend.getId(),
                    user.getId());
        } else {
            jdbcTemplate.update("INSERT INTO USER_FRIEND VALUES (?, ?, ?)",
                    user.getId(), friend.getId(), FriendshipStatus.PENDING.toString());
        }
    }

    @Override
    public void deleteFromFriends(User user, User friend) {
        Map<Integer, FriendshipStatus> userFriendSet = user.getFriendSet();
        Map<Integer, FriendshipStatus> friendFriendSet = friend.getFriendSet();
        if (!userFriendSet.containsKey(friend.getId())) {
            throw new EntryNotFoundException("Пользователь с этим id не найден в списке друзей!");
        }
        jdbcTemplate.update("DELETE FROM USER_FRIEND WHERE USER_ID = ? AND FRIEND_ID = ?",
                user.getId(), friend.getId());
        if (friendFriendSet.containsKey(user.getId())) {
            jdbcTemplate.update("UPDATE USER_FRIEND SET FRIENDSHIP_STATUS = ? WHERE " +
                    "USER_ID = ? AND FRIEND_ID = ?",
                    FriendshipStatus.PENDING.toString(), friend.getId(), user.getId());
        }
    }

    @Override
    public List<User> getAllUsers() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from USERS");
        List<User> allUsers = new ArrayList<>();
        while (userRows.next()) {
            User user = mapUserFromDb(userRows);
            mapUserFriends(user);
            allUsers.add(user);
        }
        return allUsers;
    }

    @Override
    public User getUserById(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "select * from USERS where USER_ID = ?", id);
        if (userRows.next()) {
            User user = mapUserFromDb(userRows);
            mapUserFriends(user);
            return user;
        } else {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        }
    }

    private User mapUserFromDb(SqlRowSet userRow) {
        User user = new User();
        user.setId(userRow.getInt("USER_ID"));
        user.setName(userRow.getString("NAME"));
        user.setLogin(userRow.getString("LOGIN"));
        java.sql.Date birthday = userRow.getDate("BIRTHDAY");
        user.setBirthday(birthday == null ? null : birthday.toLocalDate());
        user.setEmail(userRow.getString("EMAIL"));
        return user;
    }

    private void mapUserFriends(User user) {
        SqlRowSet userFriends = jdbcTemplate.queryForRowSet(
                "select * from USER_FRIEND where USER_ID = ?", user.getId());
        Map<Integer, FriendshipStatus> friendSet = new HashMap<>();
        while (userFriends.next()) {
            friendSet.put(userFriends.getInt("FRIEND_ID"),
                    FriendshipStatus.valueOf(userFriends.getString("FRIENDSHIP_STATUS")));
        }
        user.setFriendSet(friendSet);
    }

    private void applyLoginToName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
