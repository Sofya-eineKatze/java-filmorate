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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM friendship");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void shouldCreateUserWhenDataIsValid() throws Exception {
        User user = new User(null, "user@example.com", "userlogin", "User Name", LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.login").value("userlogin"))
                .andExpect(jsonPath("$.name").value("User Name"));
    }

    @Test
    void shouldCreateUserWithEmptyNameUseLogin() throws Exception {
        User user = new User(null, "noname@example.com", "nonamelogin", "", LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("nonamelogin"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        User user1 = new User(null, "user1@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1));
        User user2 = new User(null, "user2@mail.ru", "login2", "Name2", LocalDate.of(2001, 2, 2));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = new User(null, "find@mail.ru", "findLogin", "Find Name", LocalDate.of(1995, 5, 5));

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = objectMapper.readValue(response, User.class);

        mockMvc.perform(get("/users/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.email").value("find@mail.ru"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = new User(null, "old@mail.ru", "oldLogin", "Old Name", LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = objectMapper.readValue(response, User.class);

        User updated = new User(created.getId(), "new@mail.ru", "newLogin", "New Name", LocalDate.of(1991, 2, 2));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@mail.ru"))
                .andExpect(jsonPath("$.login").value("newLogin"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = new User(null, "delete@mail.ru", "deleteLogin", "Delete Name", LocalDate.of(2000, 1, 1));

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = objectMapper.readValue(response, User.class);

        mockMvc.perform(delete("/users/{id}", created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddFriend() throws Exception {
        // Создаём первого пользователя
        User user1 = new User(null, "user1@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1));
        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer id1 = objectMapper.readValue(response1, User.class).getId();

        // Создаём второго пользователя
        User user2 = new User(null, "user2@mail.ru", "login2", "Name2", LocalDate.of(2001, 2, 2));
        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer id2 = objectMapper.readValue(response2, User.class).getId();

        // Пользователь 1 отправляет заявку пользователю 2
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isOk());

        // Пользователь 2 подтверждает дружбу (отправляет ответную заявку)
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id2, id1))
                .andExpect(status().isOk());

        // Проверяем список друзей пользователя 1
        mockMvc.perform(get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id2));

        // Проверяем список друзей пользователя 2
        mockMvc.perform(get("/users/{id}/friends", id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id1));
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        // Создаём трёх пользователей
        User user1 = new User(null, "common1@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1));
        User user2 = new User(null, "common2@mail.ru", "login2", "Name2", LocalDate.of(2001, 2, 2));
        User common = new User(null, "common@mail.ru", "login3", "Name3", LocalDate.of(2002, 3, 3));

        String r1 = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1))).andReturn().getResponse().getContentAsString();
        String r2 = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2))).andReturn().getResponse().getContentAsString();
        String rc = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(common))).andReturn().getResponse().getContentAsString();

        Integer id1 = objectMapper.readValue(r1, User.class).getId();
        Integer id2 = objectMapper.readValue(r2, User.class).getId();
        Integer idCommon = objectMapper.readValue(rc, User.class).getId();

        // Пользователь 1 добавляет общего друга
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id1, idCommon)).andExpect(status().isOk());
        // Пользователь 2 добавляет общего друга
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id2, idCommon)).andExpect(status().isOk());

        // Общий друг подтверждает дружбу с пользователем 1
        mockMvc.perform(put("/users/{id}/friends/{friendId}", idCommon, id1)).andExpect(status().isOk());
        // Общий друг подтверждает дружбу с пользователем 2
        mockMvc.perform(put("/users/{id}/friends/{friendId}", idCommon, id2)).andExpect(status().isOk());

        // Проверяем общих друзей пользователей 1 и 2
        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", id1, id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(idCommon));
    }

    @Test
    void shouldRemoveFriend() throws Exception {
        // Создаём двух пользователей
        User user1 = new User(null, "remove1@mail.ru", "removeLogin1", "Name1", LocalDate.of(2000, 1, 1));
        User user2 = new User(null, "remove2@mail.ru", "removeLogin2", "Name2", LocalDate.of(2001, 2, 2));

        String r1 = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1))).andReturn().getResponse().getContentAsString();
        String r2 = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2))).andReturn().getResponse().getContentAsString();

        Integer id1 = objectMapper.readValue(r1, User.class).getId();
        Integer id2 = objectMapper.readValue(r2, User.class).getId();

        // Добавляем в друзья
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id1, id2)).andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id2, id1)).andExpect(status().isOk());

        // Проверяем что друзья есть
        mockMvc.perform(get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Удаляем из друзей
        mockMvc.perform(delete("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isOk());

        // Проверяем что друзей нет
        mockMvc.perform(get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() throws Exception {
        User user = new User(null, "invalid-email", "login", "Name", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithBlankLogin() throws Exception {
        User user = new User(null, "user@mail.ru", "", "Name", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() throws Exception {
        User user = new User(null, "user@mail.ru", "login", "Name", LocalDate.of(2030, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddSelfAsFriend() throws Exception {
        User user = new User(null, "self@mail.ru", "selfLogin", "Self Name", LocalDate.of(2000, 1, 1));

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer id = objectMapper.readValue(response, User.class).getId();

        mockMvc.perform(put("/users/{id}/friends/{friendId}", id, id))
                .andExpect(status().isBadRequest());
    }
}