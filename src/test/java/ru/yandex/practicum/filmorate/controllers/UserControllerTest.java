package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class UserControllerTest {

    private static final String ADDRESS = "http://localhost:";
    private static final String ENDPOINT = "/users";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserController controller;
    static User user;
    static User friendUser;
    static User updatedUser;
    static User badUser;
    static URI url;

    @BeforeEach
    void init() {
        url = URI.create(ADDRESS + port + ENDPOINT);

        user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("nagibator");
        user.setName("Pyotr");
        user.setBirthday(LocalDate.of(1980, 10, 15));

        friendUser = new User();
        friendUser.setEmail("y@ndex.ru");
        friendUser.setLogin("imafriend");
        friendUser.setName("friendlyname");
        friendUser.setBirthday(LocalDate.of(1984, 3, 10));

        updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("mail@mail.ru");
        updatedUser.setLogin("Nagibator80");
        updatedUser.setName("Pyotr");
        updatedUser.setBirthday(LocalDate.of(1980, 10, 15));

        badUser = new User();
        badUser.setEmail("e@mail.com");
        badUser.setLogin("adveritae");
        badUser.setBirthday(LocalDate.of(1990, 1, 5));
    }

    @Test
    void shouldReturnUserList() {
        restTemplate.postForObject(url, user, String.class);
        assertThat(this.restTemplate.getForObject(url, List.class)).isNotEmpty();
    }

    @Test
    void shouldCreateUser() {
        ResponseEntity<String> response = restTemplate.postForEntity(url, user, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldUpdateUser() {
        restTemplate.postForObject(url, user, String.class);
        HttpEntity<User> updatedUserRequest = new HttpEntity<>(updatedUser);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT,
                updatedUserRequest, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldAddToFriends() {
        restTemplate.postForObject(url, user, String.class);
        restTemplate.postForObject(url, friendUser, String.class);
        ResponseEntity<String> response = restTemplate.exchange(url + "/1/friends/2",
                HttpMethod.PUT, new HttpEntity<>(null), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldDeleteFromFriends() {
        restTemplate.postForObject(url, user, String.class);
        restTemplate.postForObject(url, friendUser, String.class);
        restTemplate.exchange(url + "/1/friends/2",
                HttpMethod.PUT, new HttpEntity<>(null), String.class);
        ResponseEntity<String> response = restTemplate.exchange(url + "/1/friends/2",
                HttpMethod.DELETE, new HttpEntity<>(null), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturnFriendList() {
        restTemplate.postForObject(url, user, String.class);
        restTemplate.postForObject(url, friendUser, String.class);
        restTemplate.exchange(url + "/1/friends/2",
                HttpMethod.PUT, new HttpEntity<>(null), String.class);
        assertThat(this.restTemplate.getForObject(url + "/1/friends", List.class)).isNotEmpty();
    }

    @Test
    void shouldReturnCommonFriendList() {
        restTemplate.postForObject(url, user, String.class);
        restTemplate.postForObject(url, friendUser, String.class);
        restTemplate.postForObject(url, friendUser, String.class);
        restTemplate.exchange(url + "/1/friends/2",
                HttpMethod.PUT, new HttpEntity<>(null), String.class);
        restTemplate.exchange(url + "/3/friends/2",
                HttpMethod.PUT, new HttpEntity<>(null), String.class);
        assertThat(this.restTemplate.getForObject(url + "/1/friends/common/3",
                List.class)).isNotEmpty();
    }

    @Test
    void shouldProveCrissCrossFriends() {
        restTemplate.postForObject(url, user, String.class);
        restTemplate.postForObject(url, friendUser, String.class);
        restTemplate.exchange(url + "/1/friends/2",
                HttpMethod.PUT, new HttpEntity<>(null), String.class);
        assertThat(this.restTemplate.getForObject(url + "/1/friends", List.class)).isNotEmpty();
        assertThat(this.restTemplate.getForObject(url + "/2/friends", List.class)).isNotEmpty();
    }

    @Test
    void shouldReturn500WhenIdPresentOnCreate() {
        badUser.setId(2);
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn404WhenNoIdInDatabaseOnUpdate() {
        badUser.setId(197);
        HttpEntity<User> badUpdateRequest = new HttpEntity<>(badUser);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT,
                badUpdateRequest, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn404WhenNoIdOnUpdate() {
        badUser.setId(0);
        HttpEntity<User> badUpdateRequest = new HttpEntity<>(badUser);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT,
                badUpdateRequest, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenEmailIsEmptyOnCreate() {
        badUser.setEmail("");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenEmailInWrongFormatOnCreate() {
        badUser.setEmail("@meil.e");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenEmptyLoginOnCreate() {
        badUser.setLogin("");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenLoginContainsWhitespaceOnCreate() {
        badUser.setLogin("ad veritae");
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenBornInFutureOnCreate() {
        badUser.setBirthday(LocalDate.now().plusDays(25));
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenJustBorn() {
        badUser.setBirthday(LocalDate.now());
        ResponseEntity<String> response = restTemplate.postForEntity(url, badUser, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldApplyLoginToNameIfNameIsEmpty() {
        User namelessUser = new User();
        namelessUser.setEmail("E@mail.ru");
        namelessUser.setLogin("ihavenoname");
        namelessUser.setName("");
        namelessUser.setBirthday(LocalDate.of(1995, 3, 20));
        restTemplate.postForObject(url, namelessUser, String.class); //добавляем namelessUser
        User[] returned = restTemplate.getForObject(url, User[].class); //получаем массив User
        User returnedUser = null;
        //итерируемся по массиву, пока не найдем namelessUser
        for (User user1 : returned) {
            if (user1.getLogin().equals(namelessUser.getLogin())) {
                returnedUser = user1;
            }
        }
        Assertions.assertNotNull(returnedUser); //проверяем, что вообще нашли его
        Assertions.assertEquals(returnedUser.getLogin(), returnedUser.getName()); //сверяем имя и логин
    }
}