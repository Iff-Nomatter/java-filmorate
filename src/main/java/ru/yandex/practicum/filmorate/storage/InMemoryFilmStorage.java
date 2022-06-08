package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        filmToUpdate.setMpa(film.getMpa());
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
    public void addLike(Film film, int userId) {
        film.getLikeSet().add(userId);
    }

    @Override
    public void deleteLike(Film film, int userId) {
        film.getLikeSet().remove(userId);
    }

    @Override
    public List<Film> getAllFilms() {
        return getAll();
    }

    @Override
    public Film getFilmById(int id) {
        return getById(id);
    }

    @Override
    public List<Film> getPopular(String genre, Integer year) {
        Predicate<Film> genreFilter;
        if (genre != null) {
            genreFilter = film -> film.getGenre().stream()
                    .anyMatch(filmGenre -> filmGenre.getName().equals(genre));
        } else {
            genreFilter = film -> true;
        }

        Predicate<Film> yearFilter;
        if (year != null) {
            yearFilter = film -> film.getReleaseDate().getYear() == year;
        } else {
            yearFilter = film -> true;
        }

        return getAllFilms().stream()
                .filter(genreFilter)
                .filter(yearFilter)
                .collect(Collectors.toList());
    }
}
