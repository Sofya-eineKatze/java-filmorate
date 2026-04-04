package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAll();

    Optional<User> getById(Integer id);

    User create(User user);

    User update(User user);

    void delete(Integer id);

    boolean exists(Integer id);

    boolean emailExists(String email);
}