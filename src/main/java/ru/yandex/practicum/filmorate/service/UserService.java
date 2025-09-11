package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        ensureNameForCreate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null || !userStorage.exists(user.getId())) {
            throw new NotFoundException("User с id " + user.getId() + " не найден");
        }
        User stored = userStorage.findById(user.getId()).orElseThrow(
                () -> new NotFoundException("User с id " + user.getId() + " не найден"));
        if (user.getEmail() != null) stored.setEmail(user.getEmail());
        if (user.getLogin() != null) stored.setLogin(user.getLogin());
        if (user.getName() != null) stored.setName(user.getName());
        if (user.getBirthday() != null) stored.setBirthday(user.getBirthday());
        userStorage.update(stored);
        log.info("Updated user {}", stored.getId());
        return stored;
    }

    public User getById(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User с id " + id + " не найден"));
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (!userStorage.exists(userId) || !userStorage.exists(friendId)) {
            throw new NotFoundException("User не найден");
        }
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("User {} and {} are now friends", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Friend {} removed from user {}", friendId, userId);
    }

    public Collection<User> getFriends(Integer userId) {
        User user = getById(userId);
        Set<Integer> friendsIds = user.getFriends();
        return friendsIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = getById(userId);
        User other = getById(otherId);

        Set<Integer> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());

        return common.stream().map(this::getById).collect(Collectors.toList());
    }

    private void ensureNameForCreate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}