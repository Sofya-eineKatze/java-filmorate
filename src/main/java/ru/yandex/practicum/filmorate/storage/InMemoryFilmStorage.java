package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private int idCounter = 1;

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        Film newFilm = new Film(
                idCounter++,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм '{}' создан с id: {}", newFilm.getName(), newFilm.getId());
        return newFilm;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм не найден");
        }
        Film updatedFilm = new Film(
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getLikes()
        );
        films.put(film.getId(), updatedFilm);
        log.info("Фильм с id {} обновлен", film.getId());
        return updatedFilm;
    }

    @Override
    public void delete(Integer id) {
        films.remove(id);
        log.info("Фильм с id {} удален", id);
    }

    @Override
    public boolean exists(Integer id) {
        return films.containsKey(id);
    }
}