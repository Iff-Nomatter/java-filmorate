package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmDirectorRowMapper implements RowMapper<FilmDirector> {

    @Override
    public FilmDirector mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmDirector filmDirector = new FilmDirector();
        filmDirector.setId(rs.getInt("GENRE_ID"));
        filmDirector.setName(rs.getString("GENRE"));
        return filmDirector;
    }
}