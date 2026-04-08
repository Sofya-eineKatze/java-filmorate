package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public void addFriend(Integer userId, Integer friendId) {
        String checkSql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            jdbcTemplate.update("UPDATE friendship SET status = true WHERE user_id = ? AND friend_id = ?", userId, friendId);
        } else {
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)", userId, friendId, true);
        }
        log.info("Дружба добавлена: {} -> {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
        log.info("Дружба удалена: {} -> {}", userId, friendId);
    }

    public void deleteAllByUserId(Integer userId) {
        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ?", userId);
    }

    public boolean existsFriendship(Integer userId, Integer friendId) {
        String sql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ? AND status = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}