package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        ensureNameForCreate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new NotFoundException("User с id null не найден");
        }

        User stored = userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User с id " + user.getId() + " не найден"));

        if (user.getEmail() != null) {
            stored.setEmail(user.getEmail());
        }
        if (user.getLogin() != null) {
            stored.setLogin(user.getLogin());
        }
        if (user.getName() != null) {
            stored.setName(user.getName());
        }
        if (user.getBirthday() != null) {
            stored.setBirthday(user.getBirthday());
        }
        ensureNameForCreate(stored);
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

        friendshipStorage.addFriend(userId, friendId);
        log.info("User {} добавил в друзья (односторонне) user {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (!userStorage.exists(userId) || !userStorage.exists(friendId)) {
            throw new NotFoundException("User не найден");
        }

        friendshipStorage.removeFriend(userId, friendId);
        log.info("User {} удалил из друзей user {}", userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("User с id " + userId + " не найден");
        }
        Set<Integer> friendsIds = friendshipStorage.getFriendsIds(userId);
        return friendsIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        if (!userStorage.exists(userId) || !userStorage.exists(otherId)) {
            throw new NotFoundException("User не найден");
        }

        Set<Integer> userFriends = friendshipStorage.getFriendsIds(userId);
        Set<Integer> otherFriends = friendshipStorage.getFriendsIds(otherId);

        Set<Integer> common = new HashSet<>(userFriends);
        common.retainAll(otherFriends);

        return common.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    private void ensureNameForCreate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}