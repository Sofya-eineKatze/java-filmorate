package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreDbStorage genreStorage;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Запрос на получение всех жанров");
        return genreStorage.getAll();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Integer id) {
        log.info("Запрос на получение жанра с id: {}", id);
        return genreStorage.getByIdOrThrow(id);
    }
}