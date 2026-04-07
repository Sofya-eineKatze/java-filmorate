package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getAll() {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                """;
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        return loadGenresAndLikes(films);
    }

    @Override
    public Optional<Film> getById(Integer id) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                WHERE f.id = ?
                """;
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        List<Film> result = loadGenresAndLikes(films);
        return Optional.of(result.get(0));
    }

    @Override
    public Film create(Film film) {
        // Проверяем, существует ли MPA рейтинг
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String checkMpaSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
            if (count == null || count == 0) {
                throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
            }
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Integer mpaId = film.getMpa() != null ? film.getMpa().getId() : null;
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            if (mpaId != null) {
                ps.setInt(5, mpaId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        updateGenres(id, film.getGenres());
        return getById(id).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        if (!exists(film.getId())) {
            throw new NotFoundException("Фильм не найден");
        }

        // Проверяем, существует ли MPA рейтинг
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String checkMpaSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
            if (count == null || count == 0) {
                throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
            }
        }

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        Integer mpaId = film.getMpa() != null ? film.getMpa().getId() : null;
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId());
        updateGenres(film.getId(), film.getGenres());
        return getById(film.getId()).orElseThrow();
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public boolean exists(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name,
                       COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);
        return loadGenresAndLikes(films);
    }

    private void updateGenres(Integer filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : genres) {
                // Проверяем, существует ли жанр
                String checkGenreSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
                Integer count = jdbcTemplate.queryForObject(checkGenreSql, Integer.class, genre.getId());
                if (count == null || count == 0) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
                jdbcTemplate.update(sql, filmId, genre.getId());
            }
        }
    }

    private List<Film> loadGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) return new ArrayList<>();
        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        String ids = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        Map<Integer, Set<Genre>> genresMap = new HashMap<>();
        Map<Integer, Set<Integer>> likesMap = new HashMap<>();
        if (!filmIds.isEmpty()) {
            String genresSql = """
                    SELECT fg.film_id, g.id, g.name
                    FROM film_genres fg
                    JOIN genres g ON fg.genre_id = g.id
                    WHERE fg.film_id IN (%s)
                    """.formatted(ids);
            jdbcTemplate.query(genresSql, rs -> {
                Integer filmId = rs.getInt("film_id");
                Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
                genresMap.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            });
            String likesSql = "SELECT film_id, user_id FROM likes WHERE film_id IN (%s)".formatted(ids);
            jdbcTemplate.query(likesSql, rs -> {
                Integer filmId = rs.getInt("film_id");
                Integer userId = rs.getInt("user_id");
                likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            });
        }
        List<Film> result = new ArrayList<>();
        for (Film oldFilm : films) {
            Film newFilm = new Film(
                    oldFilm.getId(),
                    oldFilm.getName(),
                    oldFilm.getDescription(),
                    oldFilm.getReleaseDate(),
                    oldFilm.getDuration(),
                    likesMap.getOrDefault(oldFilm.getId(), new HashSet<>()),
                    genresMap.getOrDefault(oldFilm.getId(), new LinkedHashSet<>()),
                    oldFilm.getMpa()
            );
            result.add(newFilm);
        }
        return result;
    }

    public static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            MpaRating mpa = null;
            if (rs.getObject("mpa_id") != null) {
                mpa = new MpaRating(rs.getInt("mpa_id"), rs.getString("mpa_name"));
            }
            return new Film(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(),
                    new LinkedHashSet<>(),
                    mpa
            );
        }
    }
}