package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return userStorage.getAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id) {
        log.info("Запрос на получение пользователя с id: {}", id);
        return userStorage.getById(id)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Начало процесса создания пользователя: {}", user.getLogin());
        validate(user);

        String finalName;
        if (user.getName() == null || user.getName().isBlank()) {
            finalName = user.getLogin();
        } else {
            finalName = user.getName();
        }

        User userWithName = new User(null, user.getEmail(), user.getLogin(), finalName, user.getBirthday());
        return userStorage.create(userWithName);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Запрос на обновление пользователя с id {}", user.getId());

        if (user.getId() == null) {
            log.warn("Ошибка обновления: не указан ID");
            throw new ValidationException("Id должен быть указан");
        }

        if (!userStorage.exists(user.getId())) {
            log.warn("Пользователь с id {} не существует", user.getId());
            throw new ValidationException("Пользователь не найден");
        }

        validate(user);
        String finalName = (user.getName() == null || user.getName().isBlank())
                ? user.getLogin()
                : user.getName();
        User updatedUser = new User(user.getId(), user.getEmail(), user.getLogin(), finalName, user.getBirthday());
        return userStorage.update(updatedUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Пользователь {} удаляет из друзей пользователя {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<User> getFriends(@PathVariable Integer id) {
        log.info("Запрос списка друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.info("Запрос общих друзей пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: email не указан");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: email '{}' не содержит символ @", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: логин не указан");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин '{}' содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.warn("Валидация не пройдена: дата рождения не указана");
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения '{}' в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}