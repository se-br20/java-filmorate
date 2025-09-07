package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Create;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Update;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAllFilms() {
        log.info("Получен список фильмов");
        return films.values();
    }

    @PostMapping
    public Film createNewFilm(@Validated(Create.class) @RequestBody Film film) {
        log.info("Создан фильм: {}", film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Validated(Update.class) @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());
            if (film.getName() != null && !film.getName().isBlank()) {
                oldFilm.setName(film.getName());
            }
            if (film.getDescription() != null && !film.getDescription().isBlank()) {
                oldFilm.setDescription(film.getDescription());
            }
            if (film.getReleaseDate() != null) {
                oldFilm.setReleaseDate(film.getReleaseDate());
            }
            if (film.getDuration() != null) {
                oldFilm.setDuration(film.getDuration());
            }
            log.info("Обновлён фильм: {}", oldFilm);
            return oldFilm;
        }
        log.warn("Ошибка валидации при корректировке фильма: {}", film);
        throw new NotFoundException("Фильма с id " + film.getId() + " нет в списке");
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
