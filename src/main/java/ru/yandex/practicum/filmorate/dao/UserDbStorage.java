package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.enumerations.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    final String USER_REQUEST = "select * from USERS where USER_ID = ?";
    final String USER_REQUEST_ALL = "select * from USERS";
    final String USER_FRIENDS_REQUEST = "select * from USER_FRIEND where USER_ID = ?";
    final String USER_INSERT = "INSERT INTO USERS (NAME, LOGIN, BIRTHDAY, EMAIL) VALUES (?, ?, ?, ?)";
    final String USER_FRIENDS_INSERT = "INSERT INTO USER_FRIEND VALUES (?, ?, ?)";
    final String USER_UPDATE = "UPDATE USERS SET NAME = ?, LOGIN = ?, BIRTHDAY = ?, EMAIL = ? " +
            "WHERE USER_ID = ?";
    final String USER_ADD_FRIEND_APPROVED = "UPDATE USER_FRIEND SET FRIENDSHIP_STATUS = ? " +
            "WHERE USER_ID = ? AND FRIEND_ID = ? OR USER_ID = ? AND FRIEND_ID = ?";
    final String USER_ADD_FRIEND_PENDING = "INSERT INTO USER_FRIEND VALUES (?, ?, ?)";
    final String USER_REMOVE_FRIEND = "DELETE FROM USER_FRIEND WHERE USER_ID = ? AND FRIEND_ID = ?";
    final String USER_REMOVE_FRIEND_FRIEND_PENDING = "UPDATE USER_FRIEND SET FRIENDSHIP_STATUS = ? " +
            "WHERE USER_ID = ? AND FRIEND_ID = ?";
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(USER_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setDate(3, java.sql.Date.valueOf(user.getBirthday()));
            ps.setString(4, user.getEmail());
            return ps;
        }, keyHolder);
        user.setId((Integer) keyHolder.getKey());
        if (user.getFriendSet() != null && !user.getFriendSet().isEmpty()) {
            addUserFriendsData(user);
        }
    }

    private void addUserFriendsData(User user) {
        Map<Integer, FriendshipStatus> friendSet = user.getFriendSet();
        for (Map.Entry<Integer, FriendshipStatus> entry : friendSet.entrySet()) {
            jdbcTemplate.update(USER_FRIENDS_INSERT,
                    user.getId(), entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void updateUser(User user) {
        getUserById(user.getId()); //проверка на наличие записи в базе
        jdbcTemplate.update(USER_UPDATE,
                user.getName(),
                user.getLogin(),
                user.getBirthday(),
                user.getEmail(),
                user.getId());
    }

    @Override
    public void addToFriends(User user, User friend) {
        if (friend.getFriendSet().containsKey(user.getId())) {
            jdbcTemplate.update(USER_ADD_FRIEND_PENDING,
                    user.getId(), friend.getId(), FriendshipStatus.APPROVED.toString());
            jdbcTemplate.update(USER_ADD_FRIEND_APPROVED,
                    FriendshipStatus.APPROVED.toString(),
                    user.getId(),
                    friend.getId(),
                    friend.getId(),
                    user.getId());
        } else {
            jdbcTemplate.update(USER_ADD_FRIEND_PENDING,
                    user.getId(), friend.getId(), FriendshipStatus.PENDING.toString());
        }
    }

    @Override
    public void deleteFromFriends(User user, User friend) {
        Map<Integer, FriendshipStatus> friendFriendSet = friend.getFriendSet();
        jdbcTemplate.update(USER_REMOVE_FRIEND,
                user.getId(), friend.getId());
        if (friendFriendSet.containsKey(user.getId())) {
            jdbcTemplate.update(USER_REMOVE_FRIEND_FRIEND_PENDING,
                    FriendshipStatus.PENDING.toString(), friend.getId(), user.getId());
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> allUsers = jdbcTemplate.query(USER_REQUEST_ALL, new UserRowMapper());
        for (User user : allUsers) {
            mapUserFriends(user);
        }
        return allUsers;
    }

    @Override
    public User getUserById(int id) {
        User user = jdbcTemplate.queryForObject(USER_REQUEST, new UserRowMapper(), id);
        if (user != null) {
            mapUserFriends(user);
        }
        return user;
    }

    private void mapUserFriends(User user) {
        SqlRowSet userFriends = jdbcTemplate.queryForRowSet(
                USER_FRIENDS_REQUEST, user.getId());
        Map<Integer, FriendshipStatus> friendSet = new HashMap<>();
        while (userFriends.next()) {
            friendSet.put(userFriends.getInt("FRIEND_ID"),
                    FriendshipStatus.valueOf(userFriends.getString("FRIENDSHIP_STATUS")));
        }
        user.setFriendSet(friendSet);
    }
}
