package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Film {
    Integer id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;

    @JsonIgnore
    Set<Integer> likes;

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration) {
        this(id, name, description, releaseDate, duration, new HashSet<>());
    }
}