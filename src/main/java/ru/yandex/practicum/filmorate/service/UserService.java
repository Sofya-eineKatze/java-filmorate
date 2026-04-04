package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        return userStorage.save(userWithName);
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя: id={}", user.getId());

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

    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Друг не найден"));

        Set<Integer> userFriends = new HashSet<>(user.getFriends());
        Set<Integer> friendFriends = new HashSet<>(friend.getFriends());

        userFriends.add(friendId);
        friendFriends.add(userId);

        User updatedUser = new User(user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), userFriends);
        User updatedFriend = new User(friend.getId(), friend.getEmail(), friend.getLogin(), friend.getName(), friend.getBirthday(), friendFriends);

        userStorage.update(updatedUser);
        userStorage.update(updatedFriend);

        log.info("Пользователь {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Друг не найден"));

        Set<Integer> userFriends = new HashSet<>(user.getFriends());
        Set<Integer> friendFriends = new HashSet<>(friend.getFriends());

        userFriends.remove(friendId);
        friendFriends.remove(userId);

        User updatedUser = new User(user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), userFriends);
        User updatedFriend = new User(friend.getId(), friend.getEmail(), friend.getLogin(), friend.getName(), friend.getBirthday(), friendFriends);

        userStorage.update(updatedUser);
        userStorage.update(updatedFriend);

        log.info("Пользователь {} и {} больше не друзья", userId, friendId);
    }

    public Set<User> getFriends(Integer userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return user.getFriends().stream()
                .map(id -> userStorage.getById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User other = userStorage.getById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());

        return commonIds.stream()
                .map(id -> userStorage.getById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toSet());
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