package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate);

        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("MERGE INTO genres (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Документальный'), (6, 'Боевик')");
        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17')");
    }

    @Test
    void shouldCreateFilm() {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void shouldGetFilmById() {
        MpaRating mpa = new MpaRating(2, "PG");
        Film film = new Film(null, "Get Film", "Description", LocalDate.of(2021, 2, 2), 90, new HashSet<>(), new HashSet<>(), mpa);
        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.getById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Get Film");
    }

    @Test
    void shouldGetAllFilms() {
        MpaRating mpa = new MpaRating(1, "G");
        filmStorage.create(new Film(null, "Film1", "Desc1", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa));
        filmStorage.create(new Film(null, "Film2", "Desc2", LocalDate.of(2021, 2, 2), 90, new HashSet<>(), new HashSet<>(), mpa));

        List<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldUpdateFilm() {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Old Name", "Old Desc", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film created = filmStorage.create(film);

        MpaRating newMpa = new MpaRating(2, "PG");
        Film updated = new Film(created.getId(), "New Name", "New Desc", LocalDate.of(2021, 2, 2), 150, new HashSet<>(), new HashSet<>(), newMpa);
        Film result = filmStorage.update(updated);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDuration()).isEqualTo(150);
        assertThat(result.getMpa().getId()).isEqualTo(2);
    }

    @Test
    void shouldDeleteFilm() {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Delete Film", "Desc", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film created = filmStorage.create(film);

        filmStorage.delete(created.getId());

        Optional<Film> found = filmStorage.getById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckFilmExists() {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Exists Film", "Desc", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film created = filmStorage.create(film);

        boolean exists = filmStorage.exists(created.getId());
        boolean notExists = filmStorage.exists(999);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldAddLike() {
        User user = userStorage.create(new User(null, "user@mail.ru", "userLogin", "User Name", LocalDate.of(2000, 1, 1)));
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Liked Film", "Desc", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film created = filmStorage.create(film);

        filmStorage.addLike(created.getId(), user.getId());

        Optional<Film> afterLike = filmStorage.getById(created.getId());
        assertThat(afterLike).isPresent();
        assertThat(afterLike.get().getLikes()).contains(user.getId());
    }

    @Test
    void shouldRemoveLike() {
        User user = userStorage.create(new User(null, "user2@mail.ru", "user2Login", "User2 Name", LocalDate.of(2000, 1, 1)));
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Unliked Film", "Desc", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film created = filmStorage.create(film);

        filmStorage.addLike(created.getId(), user.getId());
        filmStorage.removeLike(created.getId(), user.getId());

        Optional<Film> afterRemove = filmStorage.getById(created.getId());
        assertThat(afterRemove).isPresent();
        assertThat(afterRemove.get().getLikes()).doesNotContain(user.getId());
    }

    @Test
    void shouldGetPopularFilms() {
        User user1 = userStorage.create(new User(null, "user1@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1)));
        User user2 = userStorage.create(new User(null, "user2@mail.ru", "login2", "Name2", LocalDate.of(2000, 1, 1)));

        MpaRating mpa = new MpaRating(1, "G");
        Film film1 = filmStorage.create(new Film(null, "Popular Film1", "Desc1", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa));
        Film film2 = filmStorage.create(new Film(null, "Popular Film2", "Desc2", LocalDate.of(2020, 1, 1), 90, new HashSet<>(), new HashSet<>(), mpa));
        Film film3 = filmStorage.create(new Film(null, "Unpopular Film", "Desc3", LocalDate.of(2020, 1, 1), 60, new HashSet<>(), new HashSet<>(), mpa));

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(2);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film2.getId());
    }

    @Test
    void shouldCreateFilmWithGenres() {
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Film With Genres", "Desc", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), genres, mpa);

        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.getById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getGenres().iterator().next().getName()).isEqualTo("Комедия");
    }
}