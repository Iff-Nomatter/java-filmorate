package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.util.List;

public interface DirectorStorage {

    void addDirector(FilmDirector director);

    void updateDirector(FilmDirector director);

    void deleteDirector(int id);

    FilmDirector getDirector(int id);

    List<FilmDirector> getAllDirectors();


}
