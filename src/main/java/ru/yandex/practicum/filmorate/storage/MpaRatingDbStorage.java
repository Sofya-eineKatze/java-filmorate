package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, new MpaRatingRowMapper());
    }

    public Optional<MpaRating> getById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, new MpaRatingRowMapper(), id);
        return ratings.isEmpty() ? Optional.empty() : Optional.of(ratings.get(0));
    }

    public MpaRating getByIdOrThrow(Integer id) {
        return getById(id).orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }

    private static class MpaRatingRowMapper implements RowMapper<MpaRating> {
        @Override
        public MpaRating mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MpaRating(rs.getInt("id"), rs.getString("name"));
        }
    }
}