package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.UserFeedRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component("eventDbStorage")
public class EventDbStorage {
    final String EVENT_INSERT = "INSERT INTO USER_FEED (USER_ID, ENTITY_ID, EVENT_TYPE, OPERATION, " +
            "EVENT_TIME) VALUES (?, ?, ?, ?, ?)";
    final String EVENT_FEED_REQUEST = "SELECT * FROM USER_FEED WHERE USER_ID = ? ORDER BY EVENT_TIME";
    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addEventToFeed(int userId, EventType eventType, Operation operation, int entityId) {
        jdbcTemplate.update(EVENT_INSERT,
                userId,
                entityId,
                eventType.toString(),
                operation.toString(),
                Instant.now().toEpochMilli());
    }

    public List<Event> getUsersFeed(int id) {
        return jdbcTemplate.query(EVENT_FEED_REQUEST, new UserFeedRowMapper(), id);
    }
}
