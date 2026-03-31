package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldCreateUserWhenDataIsValid() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);
        assertEquals(user.getEmail(), createdUser.getEmail());
    }

    @Test
    void shouldUseLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);
        assertEquals("login", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsMissing() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAtSymbol() {
        User user = new User();
        user.setEmail("invalidemail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("");
        user.setName("name");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginHasSpaces() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("login with spaces");
        user.setName("name");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }
}