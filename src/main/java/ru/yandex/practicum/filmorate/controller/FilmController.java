package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAllFilms() {
        log.info("Получен список фильмов");
        return films.values();
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Ошибка валидации при создании фильма: {}", film);
            throw new ValidationException("Дата релиза должна быть позже 28 " + "декабря 1895 года");
        }
        log.info("Создан фильм: {}", film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }


    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка валидации при корректировке фильма: {}", film);
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());
            if (film.getName() != null && !film.getName().isBlank()) {
                oldFilm.setName(film.getName());
            }
            if (film.getDescription() != null && !film.getDescription().isBlank()) {
                oldFilm.setDescription(film.getDescription());
            }
            if (film.getReleaseDate() != null) {
                if (film.getReleaseDate().isBefore(minReleaseDate)) {
                    log.warn("Ошибка валидации при корректировке фильма: {}", film);
                    throw new ValidationException("Дата релиза должна быть позже 28 декабря 1895 года");
                }
                oldFilm.setReleaseDate(film.getReleaseDate());
            }
            if (film.getDuration() != null && film.getDuration() > 0) {
                oldFilm.setDuration(film.getDuration());
            }
            log.info("Обновлён фильм: {}", oldFilm);
            return oldFilm;
        }
        log.warn("Ошибка валидации при корректировке фильма: {}", film);
        throw new ValidationException("Фильма с id " + film.getId() + " нет в списке");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
