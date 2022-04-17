package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;


class UserControllerTest {

    UserController controller = new UserController();
    static User user;
    static User updatedUser;

    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("nagibator");
        user.setName("Pyotr");
        user.setBirthday(LocalDate.of(1980, 10, 15));

        updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("mail@mail.ru");
        updatedUser.setLogin("Nagibator80");
        updatedUser.setName("Pyotr");
        updatedUser.setBirthday(LocalDate.of(1980, 10, 15));

        controller.create(user);
    }

    @Test
    void shouldReturnUserList() {
        Assert.notEmpty(controller.getAll(), "Вернулся пустой список, так быть не должно!");
    }

    @Test
    void shouldCreateUser() {
        user.setId(0);
        user.setLogin("KirovReporting");
        Assertions.assertEquals(ResponseEntity.ok("verified"), controller.create(user));
    }

    @Test
    void shouldUpdateUser() {
        controller.update(updatedUser);
        List<User> userList = controller.getAll();
        User checkUser = null;
        for (User user1 : userList) {
            if (user1.equals(updatedUser)) {
                checkUser = user1;
            }
        }
        Assertions.assertNotNull(checkUser);
    }

    @Test
    void shouldThrowExceptionWhenOneOfParametersIsBad() {
        User badUser = new User();
        badUser.setId(2);
        badUser.setEmail("");
        badUser.setLogin("adveritae");
        badUser.setBirthday(LocalDate.of(1990, 1, 5));
        ResponseEntity<String> idPresentOnCreate = controller.create(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, idPresentOnCreate.getStatusCode());

        badUser.setId(584);
        ResponseEntity<String> noIdInDatabaseOnUpdate = controller.update(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, noIdInDatabaseOnUpdate.getStatusCode());

        badUser.setId(0);
        ResponseEntity<String> noIdOnUpdate = controller.update(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, noIdOnUpdate.getStatusCode());

        ResponseEntity<String> emptyEmail = controller.create(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, emptyEmail.getStatusCode());

        badUser.setEmail("badusergmail.com");
        ResponseEntity<String> emailWithoutSpecialSymbol = controller.create(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, emailWithoutSpecialSymbol.getStatusCode());

        badUser.setEmail("baduser@gmail.com");
        badUser.setLogin("");
        ResponseEntity<String> emptyLogin = controller.create(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, emptyLogin.getStatusCode());

        badUser.setLogin("ad veritae");
        ResponseEntity<String> loginWithSpaceChar = controller.create(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, loginWithSpaceChar.getStatusCode());

        badUser.setLogin("adveritae");
        badUser.setBirthday(LocalDate.now().plusDays(25));
        ResponseEntity<String> bornInFuture = controller.create(badUser);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, bornInFuture.getStatusCode());
    }

    @Test
    void shouldApplyLoginToNameIfNameIsEmpty() {
        User namelessUser = new User();
        namelessUser.setEmail("E@mail.ru");
        namelessUser.setLogin("ihavenoname");
        namelessUser.setName("");
        namelessUser.setBirthday(LocalDate.of(1995, 3, 20));

        controller.create(namelessUser);
        Assertions.assertEquals(namelessUser.getLogin(), namelessUser.getName());
    }
}