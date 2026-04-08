package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipDbStorage friendshipStorage;

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
        friendshipStorage.deleteAllByUserId(userId);
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
        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!userStorage.exists(friendId)) {
            throw new NotFoundException("Друг не найден");
        }
        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Set<User> getFriends(Integer userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.getFriends(userId);
    }

    public Set<User> getCommonFriends(Integer userId, Integer otherId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!userStorage.exists(otherId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.getCommonFriends(userId, otherId);
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