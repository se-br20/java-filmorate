package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    private Film buildFilm() {
        Film film = new Film();
        film.setName("Test film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        return film;
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
}
