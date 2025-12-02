package ru.yandex.practicum.filmorate.storage.friendship;

import java.util.Set;

public interface FriendshipStorage {

    void addFriend(Integer userId, Integer friendId);

    void removeFriend(Integer userId, Integer friendId);

    Set<Integer> getFriendsIds(Integer userId);
}
