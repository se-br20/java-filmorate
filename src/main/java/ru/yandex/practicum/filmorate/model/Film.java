package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    @Null(groups = Create.class, message = "Id не должен передаваться при создании")
    @NotNull(groups = Update.class, message = "Id обязателен при обновлении")
    private Integer id;

    @NotBlank(groups = Create.class, message = "Название фильма не может быть пустым")
    private String name;

    @NotBlank(groups = Create.class, message = "Описание фильма не может быть пустым")
    @Size(max = 200, groups = {Create.class, Update.class}, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(groups = Create.class, message = "Укажите дату релиза")
    private LocalDate releaseDate;

    @NotNull(groups = Create.class, message = "Необходимо указать продолжительность фильма")
    @Positive(groups = {Create.class, Update.class}, message = "Продолжительность фильма должна быть больше 0")
    private Integer duration;

    private Set<Integer> likes = new HashSet<>();

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @AssertTrue(groups = {Create.class, Update.class}, message = "Дата релиза должна быть позже 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return true;
        }
        return !releaseDate.isBefore(MIN_RELEASE_DATE);
    }
}
