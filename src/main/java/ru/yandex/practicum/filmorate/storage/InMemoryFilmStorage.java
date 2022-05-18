package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component
public class InMemoryFilmStorage extends InMemoryStorage<Film> implements FilmStorage {

    @Override
    public void updateParameters(Film film) {
        Film filmToUpdate = entries.get(film.getId());
        filmToUpdate.setName(film.getName());
        filmToUpdate.setDescription(film.getDescription());
        filmToUpdate.setReleaseDate(film.getReleaseDate());
        filmToUpdate.setDuration(film.getDuration());
        filmToUpdate.setGenre(film.getGenre());
        filmToUpdate.setRating(film.getRating());
    }

    @Override
    public void addFilm(Film film) {
        addEntry(film);
    }

    @Override
    public void updateFilm(Film film) {
        updateEntry(film);
    }

    @Override
    public void deleteFilm(int id) {
        deleteEntry(id);
    }

    @Override
    public List<Film> getAllFilms() {
        return getAll();
    }

    @Override
    public Film getFilmById(int id) {
        return getById(id);
    }
}
