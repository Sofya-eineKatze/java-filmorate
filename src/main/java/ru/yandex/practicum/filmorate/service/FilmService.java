package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: {}", film.getName());
        validate(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма с id: {}", film.getId());

        if (film.getId() == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }

        if (!filmStorage.exists(film.getId())) {
            throw new NotFoundException("Фильм не найден");
        }

        validate(film);
        return filmStorage.update(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Set<Integer> likes = new HashSet<>(film.getLikes());
        if (likes.add(userId)) {
            Film updatedFilm = new Film(
                    film.getId(), film.getName(), film.getDescription(),
                    film.getReleaseDate(), film.getDuration(), likes
            );
            filmStorage.update(updatedFilm);
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Set<Integer> likes = new HashSet<>(film.getLikes());
        if (likes.remove(userId)) {
            Film updatedFilm = new Film(
                    film.getId(), film.getName(), film.getDescription(),
                    film.getReleaseDate(), film.getDuration(), likes
            );
            filmStorage.update(updatedFilm);
            log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Описание не может быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}