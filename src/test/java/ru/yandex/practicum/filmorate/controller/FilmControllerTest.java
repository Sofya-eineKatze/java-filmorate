package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17')");
        jdbcTemplate.execute("MERGE INTO genres (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Документальный'), (6, 'Боевик')");
    }

    @Test
    void shouldAddFilmWhenDataIsValid() throws Exception {
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Inception", "Great movie about dreams",
                LocalDate.of(2010, 7, 16), 148, new HashSet<>(), genres, mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Inception"))
                .andExpect(jsonPath("$.duration").value(148))
                .andExpect(jsonPath("$.mpa.id").value(1))
                .andExpect(jsonPath("$.genres.length()").value(1));
    }

    @Test
    void shouldGetAllFilms() throws Exception {
        MpaRating mpa = new MpaRating(1, "G");
        Film film1 = new Film(null, "Film1", "Desc1", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film film2 = new Film(null, "Film2", "Desc2", LocalDate.of(2021, 2, 2), 90, new HashSet<>(), new HashSet<>(), mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetFilmById() throws Exception {
        MpaRating mpa = new MpaRating(2, "PG");
        Film film = new Film(null, "Find Me", "Find this film",
                LocalDate.of(2022, 3, 3), 110, new HashSet<>(), new HashSet<>(), mpa);

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film created = objectMapper.readValue(response, Film.class);

        mockMvc.perform(get("/films/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Find Me"));
    }

    @Test
    void shouldUpdateFilm() throws Exception {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Old Name", "Old Desc",
                LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film created = objectMapper.readValue(response, Film.class);

        MpaRating newMpa = new MpaRating(2, "PG");
        Film updated = new Film(created.getId(), "New Name", "New Desc",
                LocalDate.of(2021, 2, 2), 150, new HashSet<>(), new HashSet<>(), newMpa);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.duration").value(150));
    }

    @Test
    void shouldAddLike() throws Exception {
        String userJson = "{\"email\":\"test@mail.ru\",\"login\":\"testLogin\",\"name\":\"Test\",\"birthday\":\"2000-01-01\"}";
        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer userId = objectMapper.readTree(userResponse).get("id").asInt();

        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Liked Film", "Desc",
                LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);

        String filmResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer filmId = objectMapper.readTree(filmResponse).get("id").asInt();

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPopularFilms() throws Exception {
        String userJson = "{\"email\":\"popular@mail.ru\",\"login\":\"popularLogin\",\"name\":\"Popular\",\"birthday\":\"2000-01-01\"}";
        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer userId = objectMapper.readTree(userResponse).get("id").asInt();

        MpaRating mpa = new MpaRating(1, "G");
        Film film1 = new Film(null, "Popular Film", "Desc1", LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);
        Film film2 = new Film(null, "Not Popular Film", "Desc2", LocalDate.of(2020, 1, 1), 90, new HashSet<>(), new HashSet<>(), mpa);

        String film1Response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer film1Id = objectMapper.readTree(film1Response).get("id").asInt();

        mockMvc.perform(put("/films/{id}/like/{userId}", film1Id, userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Popular Film"));
    }

    @Test
    void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Too Old Film", "Desc",
                LocalDate.of(1800, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateFilmWithEmptyName() throws Exception {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "", "Desc",
                LocalDate.of(2020, 1, 1), 120, new HashSet<>(), new HashSet<>(), mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }
}