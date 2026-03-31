package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film.getName());
        validate(film);
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм '{}' успешно добавлен с id: {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id: {}", film.getId());

        if (film.getId() == null) {
            log.warn("Ошибка обновления: id фильма не указан");
            throw new ValidationException("Id фильма должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Ошибка обновления: фильм с id {} не найден", film.getId());
            throw new ValidationException("Фильм для обновления не найден");
        }

        validate(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} успешно обновлен", film.getId());
        return film;
    }

    private void validate(Film film) {
        // Проверка названия
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }

        // Проверка описания
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Валидация не пройдена: описание фильма превышает {} символов", MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Описание не может быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        // Проверка даты релиза
        if (film.getReleaseDate() == null) {
            log.warn("Валидация не пройдена: дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Проверка продолжительности
        if (film.getDuration() == null) {
            log.warn("Валидация не пройдена: продолжительность фильма не указана");
            throw new ValidationException("Продолжительность фильма должна быть указана");
        }

        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма {} не положительная", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}