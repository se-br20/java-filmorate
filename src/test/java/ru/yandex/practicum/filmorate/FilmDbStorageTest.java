package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate; // <-- вот это нужно для createUser()

    private Film buildFilm() {
        Film film = new Film();
        film.setName("Test film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1); // есть в data.sql
        film.setMpa(mpa);

        return film;
    }

    private Integer createUser() {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, "user@mail.ru");
            ps.setString(2, "user");
            ps.setString(3, "User");
            ps.setDate(4, Date.valueOf(LocalDate.of(2000, 1, 1)));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Test
    void testCreateAndFindById() {
        Film film = buildFilm();

        Film created = filmStorage.create(film);

        var found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("id", created.getId())
                                .hasFieldOrPropertyWithValue("name", "Test film")
                );
    }

    @Test
    void testUpdateFilm() {
        Film film = buildFilm();
        Film created = filmStorage.create(film);

        created.setName("Updated name");
        created.setDescription("Updated description");
        created.setDuration(150);

        filmStorage.update(created);

        var found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Updated name")
                                .hasFieldOrPropertyWithValue("description", "Updated description")
                                .hasFieldOrPropertyWithValue("duration", 150)
                );
    }

    @Test
    void testFindAllContainsCreatedFilm() {
        Film film1 = buildFilm();
        film1.setName("Film 1");
        Film created1 = filmStorage.create(film1);

        Film film2 = buildFilm();
        film2.setName("Film 2");
        Film created2 = filmStorage.create(film2);

        Collection<Film> all = filmStorage.findAll();

        assertThat(all)
                .extracting(Film::getId)
                .contains(created1.getId(), created2.getId());
    }

    @Test
    void testExists() {
        Film film = buildFilm();
        Film created = filmStorage.create(film);

        assertThat(filmStorage.exists(created.getId())).isTrue();
        assertThat(filmStorage.exists(9999)).isFalse();
    }

    @Test
    void testAddLike() {
        Film film = buildFilm();
        Film createdFilm = filmStorage.create(film);
        Integer userId = createUser();

        filmStorage.addLike(createdFilm.getId(), userId);

        var found = filmStorage.findById(createdFilm.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    Set<Integer> likes = f.getLikes();
                    assertThat(likes).contains(userId);
                });
    }

    @Test
    void testRemoveLike() {
        Film film = buildFilm();
        Film createdFilm = filmStorage.create(film);
        Integer userId = createUser();

        filmStorage.addLike(createdFilm.getId(), userId);
        filmStorage.removeLike(createdFilm.getId(), userId);

        var found = filmStorage.findById(createdFilm.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    Set<Integer> likes = f.getLikes();
                    assertThat(likes).doesNotContain(userId);
                });
    }
}
