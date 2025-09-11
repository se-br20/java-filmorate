package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null || !filmStorage.exists(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        Film stored = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        if (film.getName() != null && !film.getName().isBlank()) stored.setName(film.getName());
        if (film.getDescription() != null && !film.getDescription().isBlank())
            stored.setDescription(film.getDescription());
        if (film.getReleaseDate() != null) stored.setReleaseDate(film.getReleaseDate());
        if (film.getDuration() != null) stored.setDuration(film.getDuration());
        filmStorage.update(stored);
        log.info("Обновлён фильм: {}", stored.getId());
        return stored;
    }

    public Film getById(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("User с id " + userId + " не найден");
        }
        Film film = getById(filmId);
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("User с id " + userId + " не найден");
        }
        Film film = getById(filmId);
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(Math.max(0, count))
                .collect(Collectors.toList());
    }
}