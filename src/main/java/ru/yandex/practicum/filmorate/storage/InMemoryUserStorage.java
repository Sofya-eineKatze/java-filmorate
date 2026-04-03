package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Map<String, Integer> emailToId = new ConcurrentHashMap<>();
    private int idCounter = 1;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        if (emailToId.containsKey(user.getEmail())) {
            log.warn("Email {} уже используется", user.getEmail());
            throw new ValidationException("Этот имейл уже используется");
        }

        User newUser = new User(
                idCounter++,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                new HashSet<>()
        );
        users.put(newUser.getId(), newUser);
        emailToId.put(newUser.getEmail(), newUser.getId());
        log.info("Пользователь создан с id: {}", newUser.getId());
        return newUser;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new NotFoundException("Пользователь не найден");
        }

        User oldUser = users.get(user.getId());

        if (!oldUser.getEmail().equals(user.getEmail())) {
            if (emailToId.containsKey(user.getEmail())) {
                log.warn("Email {} уже используется", user.getEmail());
                throw new ValidationException("Этот имейл уже используется");
            }
            emailToId.remove(oldUser.getEmail());
            emailToId.put(user.getEmail(), user.getId());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id {} обновлен", user.getId());
        return user;
    }

    @Override
    public void delete(Integer id) {
        User user = users.remove(id);
        if (user != null) {
            emailToId.remove(user.getEmail());
            // Удаляем пользователя из друзей у всех
            for (User u : users.values()) {
                if (u.getFriends().contains(id)) {
                    Set<Integer> newFriends = new HashSet<>(u.getFriends());
                    newFriends.remove(id);
                    User updated = new User(
                            u.getId(), u.getEmail(), u.getLogin(),
                            u.getName(), u.getBirthday(), newFriends
                    );
                    users.put(u.getId(), updated);
                }
            }
            log.info("Пользователь с id {} удален", id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        return users.containsKey(id);
    }

    @Override
    public boolean emailExists(String email) {
        return emailToId.containsKey(email);
    }
}