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
public class User {
    Integer id;
    String email;
    String login;
    String name;
    LocalDate birthday;

    @JsonIgnore
    Set<Integer> friends;

    // Конструктор без friends для удобства
    public User(Integer id, String email, String login, String name, LocalDate birthday) {
        this(id, email, login, name, birthday, new HashSet<>());
    }
}