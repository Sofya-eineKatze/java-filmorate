package ru.yandex.practicum.filmorate.model;

import lombok.Value;
import java.time.LocalDate;

@Value
public class User {
    Integer id;
    String email;
    String login;
    String name;
    LocalDate birthday;
}