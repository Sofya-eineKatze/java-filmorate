package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Начало процесса создания пользователя: {}", user.getLogin());
        validate(user);

        // Проверка email на уникальность
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            log.warn("Ошибка создания: email {} уже занят", user.getEmail());
            throw new ValidationException("Этот имейл уже используется");
        }

        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно создан с id {}", user.getLogin(), user.getId());
        return user;
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

        // Валидируем обновленные данные
        validate(user);

        // Обновляем все поля
        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setName(user.getName());
        existingUser.setBirthday(user.getBirthday());

        log.info("Пользователь с id {} успешно обновлен", user.getId());
        return existingUser;
    }

    private void validate(User user) {
        // Проверка email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: email не указан");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: email '{}' не содержит символ @", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        // Проверка логина
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: логин не указан");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин '{}' содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        // Проверка имени (если пустое - используем логин)
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        // Проверка даты рождения
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