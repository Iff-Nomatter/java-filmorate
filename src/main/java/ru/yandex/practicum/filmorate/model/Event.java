package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;

@Data
public class Event {
    private int eventId;
    private int userId;
    private EventType eventType;
    private Operation operation;
    private int entityId;
    private long timestamp;
}
