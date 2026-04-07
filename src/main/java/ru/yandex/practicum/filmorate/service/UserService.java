package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(Integer id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public User createUser(User user) {
        log.info("Создание пользователя: {}", user.getLogin());
        validate(user);

        String finalName = (user.getName() == null || user.getName().isBlank())
                ? user.getLogin()
                : user.getName();

        if (userStorage.emailExists(user.getEmail())) {
            throw new ValidationException("Этот имейл уже используется");
        }

        User userWithName = new User(null, user.getEmail(), user.getLogin(), finalName, user.getBirthday());
        return userStorage.create(userWithName);
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя с id: {}", user.getId());

        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!userStorage.exists(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }

        validate(user);

        String finalName = (user.getName() == null || user.getName().isBlank())
                ? user.getLogin()
                : user.getName();

        User updatedUser = new User(user.getId(), user.getEmail(), user.getLogin(), finalName, user.getBirthday());
        return userStorage.update(updatedUser);
    }

    public void deleteUser(Integer userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? OR friend_id = ?", userId, userId);
        userStorage.delete(userId);
        log.info("Пользователь с id {} удален", userId);
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!userStorage.exists(friendId)) {
            throw new NotFoundException("Друг не найден");
        }

        // Создаём или обновляем записи в обе стороны со статусом true
        jdbcTemplate.update("MERGE INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)", userId, friendId, true);
        jdbcTemplate.update("MERGE INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)", friendId, userId, true);

        log.info("Пользователь {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        // Проверяем, существует ли дружба
        String checkSql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == null || count == 0) {
            throw new NotFoundException("Дружба не найдена");
        }

        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", friendId, userId);
        log.info("Пользователь {} и {} больше не друзья", userId, friendId);
    }

    public Set<User> getFriends(Integer userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sql = """
            SELECT u.* FROM users u
            JOIN friendship f ON u.id = f.friend_id
            WHERE f.user_id = ? AND f.status = true
            """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate()
        ), userId));
    }

    public Set<User> getCommonFriends(Integer userId, Integer otherId) {
        String sql = """
            SELECT u.* FROM users u
            WHERE u.id IN (
                SELECT f1.friend_id FROM friendship f1
                WHERE f1.user_id = ? AND f1.status = true
            ) AND u.id IN (
                SELECT f2.friend_id FROM friendship f2
                WHERE f2.user_id = ? AND f2.status = true
            )
            """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate()
        ), userId, otherId));
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}