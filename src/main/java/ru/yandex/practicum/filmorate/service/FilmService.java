package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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
}