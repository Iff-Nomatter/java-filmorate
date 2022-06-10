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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;
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
