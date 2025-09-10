package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserValidationTest {

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

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void validUser_noViolations() {
        User user = createValidUser();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void emailBlank_shouldHaveViolation() {
        User user = createValidUser();
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void emailInvalid_shouldHaveViolation() {
        User user = createValidUser();
        user.setEmail("invalid-email-without-at");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        || v.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void emailNull_shouldHaveViolation() {
        User user = createValidUser();
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void loginWithSpace_shouldHaveViolation() {
        User user = createValidUser();
        user.setLogin("bad login");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
    }

    @Test
    void loginNull_shouldHaveViolation() {
        User user = createValidUser();
        user.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
    }

    @Test
    void birthdayInFuture_shouldHaveViolation() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday")
                        || v.getMessage().toLowerCase().contains("дата рождения")
                        || v.getMessage().toLowerCase().contains("past"));
    }

    @Test
    void birthdayNull_shouldHaveViolation() {
        User user = createValidUser();
        user.setBirthday(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday"));
    }
}