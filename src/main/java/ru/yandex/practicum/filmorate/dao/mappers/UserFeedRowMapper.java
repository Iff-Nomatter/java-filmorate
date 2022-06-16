package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserFeedRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();

        event.setEventId(rs.getInt("EVENT_ID"));
        event.setUserId(rs.getInt("USER_ID"));
        event.setEntityId(rs.getInt("ENTITY_ID"));
        event.setEventType(EventType.valueOf(rs.getString("EVENT_TYPE")));
        event.setOperation(Operation.valueOf(rs.getString("OPERATION")));
        event.setTimestamp(rs.getLong("EVENT_TIME"));

        return event;
    }
}
