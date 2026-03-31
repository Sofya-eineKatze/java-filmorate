package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldAddFilmWhenDataIsValid() {
        Film film = new Film("Inception", "Description", LocalDate.of(2010, 7, 16), 148);
        Film addedFilm = filmController.addFilm(film);
        assertEquals(film.getName(), addedFilm.getName());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = new Film("", "Description", LocalDate.now(), 100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        String longDescription = "a".repeat(201);
        Film film = new Film("Name", longDescription, LocalDate.now(), 100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeCinemaBirthday() {
        // День рождения кино 28.12.1895. Проверяем граничное условие (за день до)
        Film film = new Film("Name", "Desc", LocalDate.of(1895, 12, 27), 100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film("Name", "Desc", LocalDate.now(), -1);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }
}
