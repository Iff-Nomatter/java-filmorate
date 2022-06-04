package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("USER_ID"));
        user.setName(rs.getString("NAME"));
        user.setLogin(rs.getString("LOGIN"));
        java.sql.Date birthday = rs.getDate("BIRTHDAY");
        user.setBirthday(birthday == null ? null : birthday.toLocalDate());
        user.setEmail(rs.getString("EMAIL"));

        return user;
    }
}
