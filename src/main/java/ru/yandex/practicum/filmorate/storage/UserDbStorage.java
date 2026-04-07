package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper());
        return loadFriendsForUsers(users);
    }

    @Override
    public Optional<User> getById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        List<User> result = loadFriendsForUsers(users);
        return Optional.of(result.get(0));
    }

    @Override
    public User create(User user) {
        if (emailExists(user.getEmail())) {
            throw new ValidationException("Этот имейл уже используется");
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return getById(id).orElseThrow();
    }

    @Override
    public User update(User user) {
        if (!exists(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }

        User oldUser = getById(user.getId()).orElseThrow();
        if (!oldUser.getEmail().equals(user.getEmail()) && emailExists(user.getEmail())) {
            throw new ValidationException("Этот имейл уже используется");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        return getById(user.getId()).orElseThrow();
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public boolean exists(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    private List<User> loadFriendsForUsers(List<User> users) {
        if (users.isEmpty()) return users;

        List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        String ids = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        Map<Integer, Set<Integer>> friendsMap = new HashMap<>();

        if (!userIds.isEmpty()) {
            String sql = "SELECT user_id, friend_id FROM friendship WHERE user_id IN (%s) AND status = true".formatted(ids);
            jdbcTemplate.query(sql, rs -> {
                Integer userId = rs.getInt("user_id");
                Integer friendId = rs.getInt("friend_id");
                friendsMap.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
            });
        }

        List<User> result = new ArrayList<>();
        for (User oldUser : users) {
            User newUser = new User(
                    oldUser.getId(),
                    oldUser.getEmail(),
                    oldUser.getLogin(),
                    oldUser.getName(),
                    oldUser.getBirthday(),
                    friendsMap.getOrDefault(oldUser.getId(), new HashSet<>())
            );
            result.add(newUser);
        }
        return result;
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate(),
                    new HashSet<>()
            );
        }
    }
}