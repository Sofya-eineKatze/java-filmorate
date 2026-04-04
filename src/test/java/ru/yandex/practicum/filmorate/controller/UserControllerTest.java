package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
    }

    @Test
    void shouldCreateUserWhenDataIsValid() {
        User user = new User(null, "mail@mail.ru", "login", "name", LocalDate.of(1990, 1, 1));
        User createdUser = userController.createUser(user);
        assertEquals(user.getEmail(), createdUser.getEmail());
    }

    @Test
    void shouldUseLoginAsNameIfNameIsEmpty() {
        User user = new User(null, "mail@mail.ru", "login", "", LocalDate.of(1990, 1, 1));
        User createdUser = userController.createUser(user);
        assertEquals("login", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsMissing() {
        User user = new User(null, "", "login", "name", LocalDate.now());
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAtSymbol() {
        User user = new User(null, "invalidemail.com", "login", "name", LocalDate.now());
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = new User(null, "mail@mail.ru", "", "name", LocalDate.now());
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginHasSpaces() {
        User user = new User(null, "mail@mail.ru", "login with spaces", "name", LocalDate.now());
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User(null, "mail@mail.ru", "login", "name", LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }
}