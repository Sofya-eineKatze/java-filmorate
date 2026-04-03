package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.getById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new ValidationException("Друг не найден"));

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
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new ValidationException("Друг не найден"));

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
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));

        return user.getFriends().stream()
                .map(id -> userStorage.getById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        User other = userStorage.getById(otherId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));

        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());

        return commonIds.stream()
                .map(id -> userStorage.getById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toSet());
    }
}