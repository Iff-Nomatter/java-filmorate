package ru.yandex.practicum.filmorate.exceptions;

public class EntryNotFoundException extends RuntimeException {
    public EntryNotFoundException() {
    }

    public EntryNotFoundException(String message) {
        super(message);
    }
}
