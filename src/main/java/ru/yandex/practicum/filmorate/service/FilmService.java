package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
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
        if (film.getId() == null) {
            throw new NotFoundException("Фильм с id null не найден");
        }
        Film stored = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        if (film.getName() != null && !film.getName().isBlank()) {
            stored.setName(film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            stored.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            stored.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null && film.getDuration() > 0) {
            stored.setDuration(film.getDuration());
        }
        if (film.getMpa() != null) {
            stored.setMpa(film.getMpa());
        }
        if (film.getGenres() != null) {
            stored.setGenres(film.getGenres());
        }

        filmStorage.update(stored);
        log.info("Обновлён фильм: {}", stored.getId());
        return stored;
    }

    public Film getById(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Integer filmId, Integer userId) {
        getById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с id " + userId + " не найден"));

        filmStorage.addLike(filmId, userId);
        log.info("User {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        getById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с id " + userId + " не найден"));

        filmStorage.removeLike(filmId, userId);
        log.info("User {} отменил лайк фильма {}", userId, filmId);
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.findMostPopular(count);
    }
}