package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Set<String> emails = new HashSet<>();
    private int idCounter = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Начало процесса создания пользователя: {}", user.getLogin());

        validate(user);

        String finalName = (user.getName() == null || user.getName().isBlank())
                ? user.getLogin()
                : user.getName();

        if (emails.contains(user.getEmail())) {
            log.warn("Ошибка создания: email {} уже занят", user.getEmail());
            throw new ValidationException("Этот имейл уже используется");
        }

        User newUser = new User(
                idCounter++,
                user.getEmail(),
                user.getLogin(),
                finalName,
                user.getBirthday()
        );

        users.put(newUser.getId(), newUser);
        emails.add(newUser.getEmail());

        log.info("Пользователь {} успешно создан с id {}", newUser.getLogin(), newUser.getId());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Запрос на обновление пользователя с id {}", user.getId());

        if (user.getId() == null) {
            log.warn("Ошибка обновления: не указан ID");
            throw new ValidationException("Id должен быть указан");
        }

        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            log.warn("Пользователь с id {} не существует", user.getId());
            throw new ValidationException("Пользователь не найден");
        }

        validate(user);

        String finalName = (user.getName() == null || user.getName().isBlank())
                ? user.getLogin()
                : user.getName();

        if (!existingUser.getEmail().equals(user.getEmail())) {
            if (emails.contains(user.getEmail())) {
                log.warn("Ошибка обновления: email {} уже занят", user.getEmail());
                throw new ValidationException("Этот имейл уже используется");
            }
            emails.remove(existingUser.getEmail());
            emails.add(user.getEmail());
        }

        User updatedUser = new User(
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                finalName,
                user.getBirthday()
        );
        users.put(user.getId(), updatedUser);

        log.info("Пользователь с id {} успешно обновлен", user.getId());
        return updatedUser;
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