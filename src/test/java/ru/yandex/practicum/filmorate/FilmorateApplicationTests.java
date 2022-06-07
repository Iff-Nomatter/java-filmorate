package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;
	private final JdbcTemplate jdbcTemplate;


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
		//проверка замены пустого имени логином при обновлении
		Assertions.assertEquals(newUserForUpdate.getLogin(), updatedUser.getName());
		Assertions.assertEquals(newUserForUpdate.getBirthday(), updatedUser.getBirthday());
		Assertions.assertEquals(newUserForUpdate.getEmail(), updatedUser.getEmail());
	}

	@Test
	public void testAddToFriends() {
		User user = userStorage.getUserById(1);
		User friend = userStorage.getUserById(2);
		userStorage.addToFriends(user, friend);
		user = userStorage.getUserById(user.getId());
		Assertions.assertTrue(user.getFriendSet().containsKey(2));
	}

	@Test
	public void testDeleteFromFriends() {
		User user = userStorage.getUserById(1);
		User friend = userStorage.getUserById(2);
		userStorage.deleteFromFriends(user, friend);
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
		filmStorage.addLike(film, 2);
		film = filmStorage.getFilmById(1);
		Assertions.assertTrue(film.getLikeSet().contains(2));
	}

	@Test
	public void testDeleteLike() {
		Film film = filmStorage.getFilmById(1);
		filmStorage.deleteLike(film, 2);
		film = filmStorage.getFilmById(1);
		Assertions.assertFalse(film.getLikeSet().contains(2));
	}

	@Test
	public void testGetAllFilms() {
		Assertions.assertNotNull(filmStorage.getAllFilms());
	}

	@Test
	public void testDeleteUser() {
		User userForDeletion = new User();
		userForDeletion.setName("someone");
		userForDeletion.setLogin("someLogin");
		userForDeletion.setEmail("some@e.mail");
		userForDeletion.setBirthday(LocalDate.of(1990, 5, 18));
		userStorage.addUser(userForDeletion);
		userStorage.addToFriends(userForDeletion, userStorage.getUserById(1));
		userStorage.addToFriends(userForDeletion, userStorage.getUserById(3));
		userStorage.deleteUser(4);
		String selectFriends = "SELECT * FROM USER_FRIEND WHERE USER_ID = ?";
		Assertions.assertFalse(jdbcTemplate.queryForRowSet(selectFriends, 4).next());
		List<User> allUsers = userStorage.getAllUsers();
		Assertions.assertEquals(3, allUsers.size());
	}
}
