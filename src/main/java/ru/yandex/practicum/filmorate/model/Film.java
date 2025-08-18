package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Integer id;
    @NotNull(message = "Необходимо название фильма")
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @NotNull(message = "Необходимо описание фильма")
    @NotBlank(message = "Описание фильма не может быть пустым")
    private String description;
    @NotNull(message = "Укажите дату релиза")
    private LocalDate releaseDate; 
    @NotNull(message = "Необходимо указать продолжительность фильма")
    @Positive(message = "Продолжительность фильма должна быть больше 0")
    private Integer duration;

}
