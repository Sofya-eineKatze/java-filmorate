package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaRatingDbStorage mpaStorage;

    @GetMapping
    public List<MpaRating> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        return mpaStorage.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable Integer id) {
        log.info("Запрос на получение рейтинга MPA с id: {}", id);
        return mpaStorage.getByIdOrThrow(id);
    }
}