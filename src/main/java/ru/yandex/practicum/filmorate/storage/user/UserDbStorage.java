package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        });
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        var users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        }, id);
        return users.stream().findFirst();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKey().intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public boolean exists(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
