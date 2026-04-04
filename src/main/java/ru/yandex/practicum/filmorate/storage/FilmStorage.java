package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> getAll();

    Optional<Film> getById(Integer id);

    Film save(Film film);

    Film update(Film film);

    void delete(Integer id);

    boolean exists(Integer id);
}