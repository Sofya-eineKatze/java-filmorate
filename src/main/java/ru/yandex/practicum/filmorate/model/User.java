package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
@AllArgsConstructor
public class User {
    Integer id;
    String email;
    String login;
    String name;
    LocalDate birthday;

    @JsonIgnore
    Set<Integer> friends;

    public User(Integer id, String email, String login, String name, LocalDate birthday) {
        this(id, email, login, name, birthday, new HashSet<>());
    }
}