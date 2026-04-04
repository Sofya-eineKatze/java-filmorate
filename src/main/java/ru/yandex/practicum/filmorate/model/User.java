package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
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

    public User(Integer id, String email, String login, String name, LocalDate birthday, Set<Integer> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = friends != null ? friends : new HashSet<>();
    }
}