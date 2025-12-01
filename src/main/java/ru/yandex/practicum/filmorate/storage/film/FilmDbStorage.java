package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.id
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            film.setGenres(new HashSet<>());
            film.setLikes(new HashSet<>());
            return film;
        });

        loadGenres(films);
        loadLikes(films);

        return films;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            film.setGenres(new HashSet<>());
            film.setLikes(new HashSet<>());
            return film;
        }, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenres(List.of(film));
        loadLikes(List.of(film));
        return Optional.of(film);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKey().intValue();
        film.setId(id);

        updateGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        updateGenres(film);
        return film;
    }

    @Override
    public boolean exists(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /* --------- служебные методы --------- */

    private void loadGenres(Collection<Film> films) {
        if (films.isEmpty()) return;
        List<Integer> ids = films.stream().map(Film::getId).toList();

        String inSql = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (""" + inSql + ")";

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        jdbcTemplate.query(sql, rs -> {
            Integer filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            filmMap.get(filmId).getGenres().add(genre);
        }, ids.toArray());
    }

    private void loadLikes(Collection<Film> films) {
        if (films.isEmpty()) return;
        List<Integer> ids = films.stream().map(Film::getId).toList();

        String inSql = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inSql + ")";

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        jdbcTemplate.query(sql, rs -> {
            Integer filmId = rs.getInt("film_id");
            Integer userId = rs.getInt("user_id");
            filmMap.get(filmId).getLikes().add(userId);
        }, ids.toArray());
    }

    private void updateGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        film.getGenres().forEach(g ->
                jdbcTemplate.update(insertSql, film.getId(), g.getId())
        );
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?) " +
                "ON CONFLICT (film_id, user_id) DO NOTHING";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }
}
