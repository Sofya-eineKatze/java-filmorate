package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MpaRatingDbStorageTest {

    @Autowired
    private MpaRatingDbStorage mpaStorage;

    @Test
    void shouldGetAllMpaRatings() {
        List<MpaRating> ratings = mpaStorage.getAll();
        assertThat(ratings).hasSize(5);
    }

    @Test
    void shouldGetMpaRatingById() {
        Optional<MpaRating> rating = mpaStorage.getById(1);
        assertThat(rating).isPresent();
        assertThat(rating.get().getName()).isEqualTo("G");
    }

    @Test
    void shouldReturnEmptyForNonExistentMpaRating() {
        Optional<MpaRating> rating = mpaStorage.getById(999);
        assertThat(rating).isEmpty();
    }
}