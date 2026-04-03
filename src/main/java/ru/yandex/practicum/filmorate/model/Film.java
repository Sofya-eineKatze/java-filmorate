package ru.yandex.practicum.filmorate.model;

import lombok.Value;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
public class Film {
    Integer id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Set<Integer> likes;

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = new HashSet<>();
    }

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration, Set<Integer> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = likes != null ? likes : new HashSet<>();
    }
}