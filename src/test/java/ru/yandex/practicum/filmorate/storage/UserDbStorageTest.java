package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate, userRowMapper);
        jdbcTemplate.execute("DELETE FROM friendship");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void shouldCreateUser() {
        User user = new User(null, "test@mail.ru", "testLogin", "Test Name", LocalDate.of(2000, 1, 1));
        User created = userStorage.create(user);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@mail.ru");
        assertThat(created.getLogin()).isEqualTo("testLogin");
    }

    @Test
    void shouldGetUserById() {
        User user = new User(null, "user@mail.ru", "userLogin", "User Name", LocalDate.of(1995, 5, 15));
        User created = userStorage.create(user);
        Optional<User> found = userStorage.getById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getEmail()).isEqualTo("user@mail.ru");
    }

    @Test
    void shouldGetAllUsers() {
        userStorage.create(new User(null, "user1@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1)));
        userStorage.create(new User(null, "user2@mail.ru", "login2", "Name2", LocalDate.of(2001, 2, 2)));
        List<User> users = userStorage.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void shouldUpdateUser() {
        User user = new User(null, "old@mail.ru", "oldLogin", "Old Name", LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);
        User updated = new User(created.getId(), "new@mail.ru", "newLogin", "New Name", LocalDate.of(1991, 2, 2));
        User result = userStorage.update(updated);
        assertThat(result.getEmail()).isEqualTo("new@mail.ru");
        assertThat(result.getLogin()).isEqualTo("newLogin");
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteUser() {
        User user = userStorage.create(new User(null, "delete@mail.ru", "deleteLogin", "Delete Name", LocalDate.of(2000, 1, 1)));
        Integer id = user.getId();
        userStorage.delete(id);
        Optional<User> found = userStorage.getById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckUserExists() {
        User user = userStorage.create(new User(null, "exists@mail.ru", "existsLogin", "Exists Name", LocalDate.of(2000, 1, 1)));
        boolean exists = userStorage.exists(user.getId());
        boolean notExists = userStorage.exists(999);
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCheckEmailExists() {
        userStorage.create(new User(null, "unique@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1)));
        boolean emailExists = userStorage.emailExists("unique@mail.ru");
        boolean emailNotExists = userStorage.emailExists("nonexistent@mail.ru");
        assertThat(emailExists).isTrue();
        assertThat(emailNotExists).isFalse();
    }
}