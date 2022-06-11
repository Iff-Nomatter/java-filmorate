package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
	private final UserService userStorage;
	private final FilmService filmStorage;
	private final EventDbStorage eventDbStorage;

	@Test
	public void testFindUserById() {
		User user = userStorage.getUserById(1);
		Assertions.assertEquals(1, user.getId());
	}

	@Test
	public void testUpdateUser() {
		User newUserForUpdate = new User();
		newUserForUpdate.setId(1);
		newUserForUpdate.setLogin("moth");
		newUserForUpdate.setBirthday(LocalDate.of(1950, 10, 30));
		newUserForUpdate.setEmail("light@bulb.clng");
		userStorage.updateUser(newUserForUpdate);

		User updatedUser = userStorage.getUserById(1);
		Assertions.assertEquals(updatedUser.getId(), newUserForUpdate.getId());
		Assertions.assertEquals(newUserForUpdate.getLogin(), updatedUser.getLogin());
		Assertions.assertEquals(newUserForUpdate.getName(), updatedUser.getName());
		Assertions.assertEquals(newUserForUpdate.getBirthday(), updatedUser.getBirthday());
		Assertions.assertEquals(newUserForUpdate.getEmail(), updatedUser.getEmail());
	}

	@Test
	public void testAddToFriends() {
		User user = userStorage.getUserById(1);
		User friend = userStorage.getUserById(2);
		userStorage.addToFriends(user.getId(), friend.getId());
		user = userStorage.getUserById(user.getId());
		Assertions.assertTrue(user.getFriendSet().containsKey(2));
	}

	@Test
	public void testDeleteFromFriends() {
		User user = userStorage.getUserById(1);
		User friend = userStorage.getUserById(2);
		userStorage.deleteFromFriends(user.getId(), friend.getId());
		user = userStorage.getUserById(user.getId());
		Assertions.assertFalse(user.getFriendSet().containsKey(2));
	}

	@Test
	public void testGetAllUsers() {
		Assertions.assertNotNull(userStorage.getAllUsers());
	}

	@Test
	public void testFindFilmById() {
		Film film = filmStorage.getFilmById(1);
		Assertions.assertEquals(1, film.getId());
	}

	@Test
	public void testUpdateFilm() {
		Film newFilmForUpdate = new Film();
		newFilmForUpdate.setId(1);
		newFilmForUpdate.setName("Pulp Fiction");
		newFilmForUpdate.setDescription("Classic Tarantino movie");
		newFilmForUpdate.setDuration(154);
		newFilmForUpdate.setReleaseDate(LocalDate.of(1995, 9, 25));
		FilmRating mpa = new FilmRating();
		mpa.setId(3);
		mpa.setName("PG-13");
		newFilmForUpdate.setMpa(mpa);
		filmStorage.updateFilm(newFilmForUpdate);

		Film updatedFilm = filmStorage.getFilmById(1);
		Assertions.assertEquals(updatedFilm.getId(), newFilmForUpdate.getId());
		Assertions.assertEquals(updatedFilm.getName(), newFilmForUpdate.getName());
		Assertions.assertEquals(updatedFilm.getDescription(), newFilmForUpdate.getDescription());
		Assertions.assertEquals(updatedFilm.getDuration(), newFilmForUpdate.getDuration());
		Assertions.assertEquals(updatedFilm.getReleaseDate(), newFilmForUpdate.getReleaseDate());
		Assertions.assertEquals(updatedFilm.getMpa(), newFilmForUpdate.getMpa());
	}

	@Test
	public void testAddLike() {
		Film film = filmStorage.getFilmById(1);
		filmStorage.addLike(film.getId(), 2);
		film = filmStorage.getFilmById(1);
		Assertions.assertTrue(film.getLikeSet().contains(2));
	}

	@Test
	public void testDeleteLike() {
		Film film = filmStorage.getFilmById(1);
		filmStorage.deleteLike(film.getId(), 2);
		film = filmStorage.getFilmById(1);
		Assertions.assertFalse(film.getLikeSet().contains(2));
	}

	@Test
	public void testGetPopularFilms() {
		//получаем фильмы
		Film filmFirst = filmStorage.getFilmById(1);
		Film filmSecond = filmStorage.getFilmById(2);
		//обнуляем лайки
		filmStorage.deleteLike(filmFirst,1);
		filmStorage.deleteLike(filmFirst,2);
		filmStorage.deleteLike(filmSecond,1);
		filmStorage.deleteLike(filmSecond,2);
		//ставим лайки
		filmStorage.addLike(filmFirst, 1);
		filmStorage.addLike(filmFirst, 2);
		filmStorage.addLike(filmSecond, 1);
		//инициализируем параметры
		String genre = null;
		Integer year = null;
		//выводим без фильтров
		Assertions.assertEquals(3, filmStorage.getPopular(genre, year).size());
		Assertions.assertEquals(1, filmStorage.getPopular(genre, year).get(0).getId());
		//выводим с фильтром по году
		year = 1991;
		Assertions.assertEquals(2, filmStorage.getPopular(genre, year).size());
		Assertions.assertEquals(2, filmStorage.getPopular(genre, year).get(0).getId());
		//выводим с фильтром по жанру
		year = null;
		genre = "Gangster movie";
		Assertions.assertEquals(2, filmStorage.getPopular(genre, year).size());
		Assertions.assertEquals(1, filmStorage.getPopular(genre, year).get(0).getId());
		//выводим с филтрами по жанру и по году
		year = 1991;
		genre = "Gangster movie";
		Assertions.assertEquals(1, filmStorage.getPopular(genre, year).size());
		Assertions.assertEquals(3, filmStorage.getPopular(genre, year).get(0).getId());
	}

	@Test
	public void testGetAllFilms() {
		Assertions.assertNotNull(filmStorage.getAllFilms());
	}

	@Test
	public void testGetUsersFeed() {
		User user = userStorage.getUserById(1);
		User friend = userStorage.getUserById(2);
		User anotherFriend = userStorage.getUserById(3);
		userStorage.addToFriends(user.getId(), friend.getId());
		userStorage.addToFriends(user.getId(), anotherFriend.getId());
		Film film = filmStorage.getFilmById(1);
		Film anotherfilm = filmStorage.getFilmById(2);
		filmStorage.addLike(film.getId(), 2);
		filmStorage.deleteLike(film.getId(), 2);
		userStorage.addToFriends(friend.getId(), anotherFriend.getId());
		filmStorage.addLike(anotherfilm.getId(), 3);
		userStorage.addToFriends(anotherFriend.getId(), friend.getId());
		log.info(eventDbStorage.getUsersFeed(1).toString());
		Assertions.assertNotNull(eventDbStorage.getUsersFeed(1));
	}
}
