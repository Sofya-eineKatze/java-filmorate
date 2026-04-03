package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    @JsonIgnore
    private Set<Integer> likes = new HashSet<>();

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration) {
        this(id, name, description, releaseDate, duration, new HashSet<>());
    }
}