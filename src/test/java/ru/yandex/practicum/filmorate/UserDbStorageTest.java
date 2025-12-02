package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testCreateAndFindById() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.create(user);

        var found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("id", created.getId())
                                .hasFieldOrPropertyWithValue("email", "test@mail.ru")
                                .hasFieldOrPropertyWithValue("login", "login")
                );
    }

    @Test
    void testUpdateUser() {
        // создаём
        User user = new User();
        user.setEmail("old@mail.ru");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        // изменяем
        created.setEmail("new@mail.ru");
        created.setLogin("newLogin");
        created.setName("New Name");

        userStorage.update(created);

        var found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("email", "new@mail.ru")
                                .hasFieldOrPropertyWithValue("login", "newLogin")
                                .hasFieldOrPropertyWithValue("name", "New Name")
                );
    }

    @Test
    void testFindAllContainsCreatedUser() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        User created2 = userStorage.create(user2);

        Collection<User> all = userStorage.findAll();

        assertThat(all)
                .extracting(User::getId)
                .contains(created1.getId(), created2.getId());
    }

    @Test
    void testExists() {
        User user = new User();
        user.setEmail("exists@mail.ru");
        user.setLogin("exists");
        user.setName("Exists");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        User created = userStorage.create(user);

        assertThat(userStorage.exists(created.getId())).isTrue();
        assertThat(userStorage.exists(9999)).isFalse();
    }
}
