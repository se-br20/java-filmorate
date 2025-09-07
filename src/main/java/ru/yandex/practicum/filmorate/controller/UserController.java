package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        log.info("Получен список пользователей");
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Создан пользователь: {}", user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", user);
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            oldUser.setEmail(user.getEmail());
            if (user.getLogin() != null && !user.getLogin().isBlank()) {
                oldUser.setLogin(user.getLogin());
            }
            if (user.getName() != null && !user.getName().isBlank()) {
                oldUser.setName(user.getName());
            }
            if (user.getBirthday() != null) {
                oldUser.setBirthday(user.getBirthday());
            }
            log.info("Обновлён пользователь: {}", oldUser);
            return oldUser;
        }
        log.warn("Ошибка валидации при обновлении пользователя: {}", user);
        throw new NotFoundException("User с id " + user.getId() + " не найден");
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
