package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApplicationTests {
	private final UserService userStorage;
	private final FilmService filmStorage;
	private final EventDbStorage eventDbStorage;
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


	@Test
	public void testGetCommonFilms() {
		// Достаю пользователей из базы
		User firstUser = userStorage.getUserById(1);
		User secondUser = userStorage.getUserById(2);
		User thirdUser = userStorage.getUserById(3);
		// Достаю фильмы из базы
		Film firstFilm = filmStorage.getFilmById(1);
		Film secondFilm = filmStorage.getFilmById(2);
		Film thirdFilm = filmStorage.getFilmById(3);
		// Первый пользователь добавляет в друзья второго
		userStorage.addToFriends(firstUser, secondUser);
		// Лайки для первого фильма
		filmStorage.addLike(firstFilm, firstUser.getId());
		filmStorage.addLike(firstFilm, secondUser.getId());
		filmStorage.addLike(firstFilm, thirdUser.getId());
		// Лайки для второго фильма
		filmStorage.addLike(secondFilm, firstUser.getId());
		filmStorage.addLike(secondFilm, secondUser.getId());
		// Лайк для третьего фильма
		filmStorage.addLike(thirdFilm, firstUser.getId());
		// Вызываем метод getCommonFilms
		List<Film> commonFilms = filmStorage.getCommonFilms(firstUser.getId(), secondUser.getId());
		// Проверяем, что в листе всего 2 фильма
		Assertions.assertEquals(2, commonFilms.size());
		// Делаем проверку того, что вернулись общие фильмы для первого и второго пользователя отсорированные по популярности
		for (int i = 0; i < commonFilms.size(); i++) {
			if (i == 0) {
				// Первому фильму поставили больше всего лайков, в лимте он идет самым первым
				Assertions.assertEquals("Pulp Friction",commonFilms.get(i).getName());
			} else if (i == 1) {
				// Следом идет второй фильм
				Assertions.assertEquals("Titanic",commonFilms.get(i).getName());
			}
		}
	}

	@Test
	public void testFilmRemove() {
		Film filmForDeletion = new Film();
		LinkedHashSet<FilmGenre> filmForDeletionGenre = new LinkedHashSet<>();
		FilmGenre genre1 = new FilmGenre();
		genre1.setId(1);
		FilmGenre genre2 = new FilmGenre();
		genre2.setId(5);
		filmForDeletionGenre.add(genre1);
		filmForDeletionGenre.add(genre2);
		filmForDeletion.setGenres(filmForDeletionGenre);
		FilmRating filmForDeletionRating = new FilmRating();
		filmForDeletionRating.setId(3);
		Set<Integer> filmForDeletionLikeSet = new HashSet<>();
		filmForDeletionLikeSet.add(1);
		filmForDeletionLikeSet.add(2);
		filmForDeletion.setMpa(filmForDeletionRating);
		filmForDeletion.setDescription("something");
		filmForDeletion.setName("whatever");
		filmForDeletion.setDuration(160);
		filmForDeletion.setReleaseDate(LocalDate.of(2000, 3, 15));
		filmStorage.addFilm(filmForDeletion);
		filmStorage.deleteFilm(3);
		List<Film> allFilms = filmStorage.getAllFilms();
		String selectLikes = "SELECT * FROM FILM_LIKE WHERE FILM_ID = ?";
		Assertions.assertFalse(jdbcTemplate.queryForRowSet(selectLikes, 3).next());
		String selectGenre = "SELECT * FROM FILM_GENRE WHERE FILM_ID = ?";
		Assertions.assertFalse(jdbcTemplate.queryForRowSet(selectGenre, 3).next());
		Assertions.assertEquals(2, allFilms.size());
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

	@Test
	public void testGetAllRatings() {
		Assertions.assertEquals(5, filmStorage.getAllRatings().size());
	}

	@Test
	public void testGetRatingById() {
		FilmRating expectedRating = new FilmRating();
		expectedRating.setId(3);
		expectedRating.setName("PG-13");
		Assertions.assertEquals(expectedRating, filmStorage.getRatingById(3));
	}

	@Test
	public void testGetAllGenres() {
		Assertions.assertEquals(6, filmStorage.getAllGenres().size());
	}

	@Test
	public void testGetGenreById() {
		FilmGenre expectedGenre = new FilmGenre();
		expectedGenre.setId(2);
		expectedGenre.setName("Драма");
		Assertions.assertEquals(expectedGenre, filmStorage.getGenreById(2));
	}
}
