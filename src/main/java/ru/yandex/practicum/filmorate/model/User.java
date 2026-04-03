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
public class User {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    @JsonIgnore
    private Set<Integer> friends = new HashSet<>();

    public User(Integer id, String email, String login, String name, LocalDate birthday) {
        this(id, email, login, name, birthday, new HashSet<>());
    }
}