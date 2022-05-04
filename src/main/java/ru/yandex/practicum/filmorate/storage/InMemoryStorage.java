package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exceptions.EntryNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.IdHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InMemoryStorage<T extends IdHolder> {
    protected final Map<Integer, T> entries = new HashMap<>();
    protected int idCounter = 1;

    void addEntry(T entry) {
        if (entry.getId() != 0) {
            throw new ValidationException("Для создания не нужен id!");
        }
        doOnCreate(entry);
        setId(entry);
        entries.put(entry.getId(), entry);
    }

    void updateEntry(T entry) {
        if (entry.getId() == 0) {
            throw new EntryNotFoundException("Для обновления записи нужен id!");
        }
        if (!entries.containsKey(entry.getId())) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + entry.getId());
        }
        updateParameters(entry);
    }

    void deleteEntry(int id) {
        entries.remove(id);
    }

    List<T> getAll() {
        return new ArrayList<>(entries.values());
    }

    T getById(int id) {
        if (!entries.containsKey(id)) {
            throw new EntryNotFoundException("В базе отсутствует запись c id: " + id);
        }
        return entries.get(id);
    }

    //обновляет параметры объекта из переданного объекта
    abstract public void updateParameters(T entry);

    //будет переопределен в User для установки в поле name содержимого поля login
    public void doOnCreate(T entry) {
    }

    //присваивает id к новому объекту перед записью в базу
    public void setId(T entry) {
        entry.setId(idCounter++);
    }
}
