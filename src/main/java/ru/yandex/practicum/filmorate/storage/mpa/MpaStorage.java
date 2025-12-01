package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {
    Collection<Mpa> findAll();

    Optional<Mpa> findById(Integer id);
}
