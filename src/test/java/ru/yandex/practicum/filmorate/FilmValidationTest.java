package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Create;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Update;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FilmValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Some film");
        film.setDescription("Short desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void validFilm_noViolations_forCreate() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isEmpty();
    }

    @Test
    void nameBlank_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setName(" ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("name"));
    }

    @Test
    void descriptionNull_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setDescription(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("description"));
    }

    @Test
    void descriptionTooLong_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setDescription("x".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("description"));
    }

    @Test
    void releaseDateNull_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("releaseDate"));
    }

    @Test
    void releaseDateBeforeMin_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().toLowerCase().contains("дата релиза"));
    }

    @Test
    void durationNull_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("duration"));
    }

    @Test
    void durationNonPositive_shouldHaveViolation_forCreate() {
        Film film = createValidFilm();
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("duration"));
    }

    @Test
    void idPresentOnCreate_shouldHaveViolation() {
        Film film = createValidFilm();
        film.setId(1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("id")
                || v.getMessage().toLowerCase().contains("id не должен"));
    }

    @Test
    void validFilm_noViolations_forUpdate() {
        Film film = createValidFilm();
        film.setId(10);
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Update.class);
        assertThat(violations).isEmpty();
    }

    @Test
    void idMissingOnUpdate_shouldHaveViolation() {
        Film film = createValidFilm();
        film.setId(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Update.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("id")
                || v.getMessage().toLowerCase().contains("id обязателен"));
    }

    @Test
    void descriptionTooLong_shouldHaveViolation_forUpdate() {
        Film film = createValidFilm();
        film.setId(5);
        film.setDescription("x".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Update.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("description"));
    }

    @Test
    void releaseDateBeforeMin_shouldHaveViolation_forUpdate() {
        Film film = createValidFilm();
        film.setId(5);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Update.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().toLowerCase().contains("дата релиза"));
    }

    @Test
    void durationNonPositive_shouldHaveViolation_forUpdate() {
        Film film = createValidFilm();
        film.setId(5);
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Update.class);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("duration"));
    }
}