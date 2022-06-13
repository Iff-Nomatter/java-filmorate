package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
	private final ReviewDbStorage reviewStorage;


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
		userStorage.addToFriends(user.getId(), friend.getId());
		//заново запрашиваем, чтобы замаппились друзья
		user = userStorage.getUserById(1);
		friend = userStorage.getUserById(2);
		Assertions.assertTrue(user.getFriendSet().containsKey(2));
		userStorage.deleteFromFriends(user.getId(), friend.getId());
		//снова запрашиваем ради обновления списка друзей
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
		filmStorage.addLike(film.getId(), 1);
		film = filmStorage.getFilmById(1);
		Assertions.assertTrue(film.getLikeSet().contains(1));
	}

	@Test
	public void testDeleteLike() {
		filmStorage.addLike(1, 2);
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
		//ставим лайки
		filmStorage.addLike(filmFirst.getId(), 1);
		filmStorage.addLike(filmFirst.getId(), 2);
		filmStorage.addLike(filmSecond.getId(), 1);
		//инициализируем параметры
		String genre = null;
		Integer year = null;
		//выводим без фильтров
		Assertions.assertEquals(4, filmStorage.getTopByLikes(10, genre, year).size());
		//выводим с фильтром по году
		year = 1991;
		Assertions.assertEquals(2, filmStorage.getTopByLikes(10, genre, year).size());
		//выводим с фильтром по жанру
		year = null;
		genre = "Мультфильм";
		Assertions.assertEquals(2, filmStorage.getTopByLikes(10, genre, year).size());
		//выводим с филтрами по жанру и по году
		year = 1991;
		genre = "Фантастика";
		Assertions.assertEquals(1, filmStorage.getTopByLikes(10, genre, year).size());
	}

	public void testSearchFilm() {
		Film film = filmStorage.getFilmById(1);
		String query = film.getName().substring(1,4).toUpperCase();
		List<Film> search = filmStorage.search(query);
		Assertions.assertTrue(search.contains(film));

		film.setName("New YYYY");
		filmStorage.updateFilm(film);
		search = filmStorage.search(query);
		Assertions.assertFalse(search.contains(film));
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
		userStorage.addToFriends(firstUser.getId(), secondUser.getId());
		// Лайки для первого фильма
		filmStorage.addLike(firstFilm.getId(), firstUser.getId());
		filmStorage.addLike(firstFilm.getId(), secondUser.getId());
		filmStorage.addLike(firstFilm.getId(), thirdUser.getId());
		// Лайки для второго фильма
		filmStorage.addLike(secondFilm.getId(), firstUser.getId());
		filmStorage.addLike(secondFilm.getId(), secondUser.getId());
		// Лайк для третьего фильма
		filmStorage.addLike(thirdFilm.getId(), firstUser.getId());
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
		Assertions.assertEquals(4, allFilms.size());
	}

	@Test
	public void testDeleteUser() {
		User userForDeletion = new User();
		userForDeletion.setName("someone");
		userForDeletion.setLogin("someLogin");
		userForDeletion.setEmail("some@e.mail");
		userForDeletion.setBirthday(LocalDate.of(1990, 5, 18));
		userStorage.addUser(userForDeletion);
		userStorage.addToFriends(userForDeletion.getId(), 1);
		userStorage.addToFriends(userForDeletion.getId(), 3);
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

	@Test
	public void createReview() {
		FilmReview review = new FilmReview();
		review.setFilmId(1);
		review.setUserId(1);
		review.setContent("Положительный отзыв на фильм");
		review.setPositive(true);
		FilmReview addReview = reviewStorage.create(review);
		Assertions.assertEquals(addReview.getContent(), review.getContent());
		Assertions.assertEquals(addReview.getFilmId(), review.getFilmId());
		Assertions.assertEquals(addReview.getUserId(), review.getUserId());
		Assertions.assertEquals(addReview.isPositive(), review.isPositive());
	}

	@Test
	public void ReviewUpdate() {
		FilmReview review = new FilmReview();

		int reviewId = addReviewForFilmId(1);

		review.setReviewId(reviewId);
		review.setContent("Обновленный текст отзыва");
		review.setFilmId(2);
		review.setUserId(2);
		review.setPositive(false);

		FilmReview updatedReview = reviewStorage.update(review);
		Assertions.assertEquals(updatedReview.getContent(), review.getContent());
		Assertions.assertEquals(updatedReview.getFilmId(), review.getFilmId());
		Assertions.assertEquals(updatedReview.getUserId(), review.getUserId());
		Assertions.assertEquals(updatedReview.isPositive(), review.isPositive());
	}

	@Test
	public void reviewAddLikeOrDislike() {
		FilmReview reviewFromStorage;
		int reviewId = addReviewForFilmId(1);

		//Ставим Like
		reviewStorage.addLike(reviewId, 1, true);
		//Получаем отзыв из базы
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();
		Assertions.assertEquals(reviewFromStorage.getUseful(), 1);

		//Ставим еще Like от другого пользователя
		reviewStorage.addLike(reviewId, 2, true);
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();
		Assertions.assertEquals(reviewFromStorage.getUseful(), 2);

		//Ставим Dislike
		reviewStorage.addLike(reviewId, 3, false);
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();
		Assertions.assertEquals(reviewFromStorage.getUseful(), 1);

		//Удаляем Like
		reviewStorage.removeLike(reviewId, 2);
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();
		Assertions.assertEquals(reviewFromStorage.getUseful(), 0);
	}

	@Test
	public void reviewRemoveLike() {
		FilmReview reviewFromStorage;
		int reviewId = addReviewForFilmId(1);

		//Ставим Like
		reviewStorage.addLike(reviewId, 1, true);

		//Ставим еще Like от другого пользователя
		reviewStorage.addLike(reviewId, 2, true);

		//Ставим еще Like от другого пользователя
		reviewStorage.addLike(reviewId, 3, true);

		//Так как отзыв имеет 3 лайка, то индекс полезности равен 3

		//Удаляем Like
		reviewStorage.removeLike(reviewId, 1);
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();

		//Индекс полезности уменьшится на 1 так как это был удален Like
		Assertions.assertEquals(reviewFromStorage.getUseful(), 2);

		//Ставим Dislike Индекс полезности уменьшится на 1
		reviewStorage.addLike(reviewId, 1, false);
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();
		Assertions.assertEquals(reviewFromStorage.getUseful(), 1);

		//Уаляем Dislike индекс увеличивается на 1
		reviewStorage.removeLike(reviewId, 1);
		reviewFromStorage = reviewStorage.get(reviewId).orElseThrow();
		Assertions.assertEquals(reviewFromStorage.getUseful(), 2);

	}

	@Test
	public void reviewGetAllCount() {
		//добавим 3 отзыва
		for (int i = 0; i < 3; i++) {
			addReviewForFilmId(1);
		}
		Assertions.assertEquals(reviewStorage.getAll(3).size(), 3);
		Assertions.assertEquals(reviewStorage.getAll(2).size(), 2);
	}

	@Test
	public void getReviewByFilmId() {
		//добавим 3 отзыва к фильму 1
		for (int i = 0; i < 3; i++) {
			addReviewForFilmId(1);
		}
		//добавим 2 отзыва к фильму 2
		for (int i = 0; i < 2; i++) {
			addReviewForFilmId(2);
		}

		Assertions.assertEquals(reviewStorage.getReviewByFilmId(1, 10).size(), 3);
		Assertions.assertEquals(reviewStorage.getReviewByFilmId(2, 10).size(), 2);
	}

	@Test
	public void reviewListSortingByUseful() {
		//добавим 3 отзыва
		for (int i = 0; i < 3; i++) {
			addReviewForFilmId(1);
		}
		// Ставим 2 Like отзыву id 3
		reviewStorage.addLike(3, 1, true);
		reviewStorage.addLike(3, 2, true);

		// Ставим 1 Like отзыву id 2
		reviewStorage.addLike(2, 1, true);

		//Отзыв id 3 на 1 месте
		Assertions.assertEquals(reviewStorage.getAll(10).get(0).getReviewId(), 3);
		Assertions.assertEquals(reviewStorage.getReviewByFilmId(1,10).get(0).getReviewId(), 3);

		//Отзыв id 2 на 2 месте
		Assertions.assertEquals(reviewStorage.getAll(10).get(1).getReviewId(), 2);
		Assertions.assertEquals(reviewStorage.getReviewByFilmId(1,10).get(1).getReviewId(), 2);

		//Отзыв id 1 на 3 месте
		Assertions.assertEquals(reviewStorage.getAll(10).get(2).getReviewId(), 1);
		Assertions.assertEquals(reviewStorage.getReviewByFilmId(1,10).get(2).getReviewId(), 1);
	}

	@Test
	public void removeReview() {
		//добавим 3 отзыва
		for (int i = 0; i < 3; i++) {
			addReviewForFilmId(1);
		}
		Assertions.assertEquals(reviewStorage.getAll(10).size(), 3);

		reviewStorage.remove(1);
		Assertions.assertEquals(reviewStorage.getAll(10).size(), 2);
	}

	private int addReviewForFilmId(int filmId) {
		FilmReview review = new FilmReview();
		review.setFilmId(filmId);
		review.setUserId(1);
		review.setContent("Положительный отзыв на фильм");
		review.setPositive(true);
		FilmReview addedReview = reviewStorage.create(review);
		return addedReview.getReviewId();
	}
}
