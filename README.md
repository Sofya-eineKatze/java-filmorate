# java-filmorate
Template repository for Filmorate project.

# Схема базы данных

![Схема базы данных](database_diagram.png)

В базе данных 7 таблиц:
- `users` — пользователи
- `films` — фильмы
- `mpa_ratings` — возрастные рейтинги (G, PG, PG-13, R, NC-17)
- `genres` — жанры
- `film_genres` — связь фильмов с жанрами
- `likes` — лайки фильмов
- `friendship` — дружба между пользователями со статусом

# Примеры запросов

# Топ N популярных фильмов
```sql
SELECT f.id, f.name, COUNT(l.user_id) as likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 10;

SELECT u.*
FROM users u
WHERE u.id IN (
    SELECT friend_id FROM friendship 
    WHERE user_id = 1 AND status = true
) 
AND u.id IN (
    SELECT friend_id FROM friendship 
    WHERE user_id = 2 AND status = true
);