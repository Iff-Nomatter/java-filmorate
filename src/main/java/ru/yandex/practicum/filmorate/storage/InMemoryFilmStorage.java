package ru.yandex.practicum.filmorate.storage;

import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

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
        filmToUpdate.setGenres(film.getGenres());
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
    public void deleteFilm(int filmId) {
        deleteEntry(filmId);
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
    public List<FilmRating> getAllRatings() {
        throw new NotYetImplementedException("Не имплементировано.");
    }

    @Override
    public FilmRating getRatingById(int ratingId) {
        throw new NotYetImplementedException("Не имплементировано.");
    }

    @Override
    public List<FilmGenre> getAllGenres() {
        throw new NotYetImplementedException("Не имплементировано.");
    }

    @Override
    public FilmGenre getGenreById(int genreId) {
        throw new NotYetImplementedException("Не имплементировано.");
    }

    @Override
    public Film getFilmById(int id) {
        return getById(id);
    }
}
