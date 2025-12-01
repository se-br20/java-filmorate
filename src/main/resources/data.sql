INSERT INTO mpa_ratings (id, name)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17') ON CONFLICT (id) DO NOTHING;

INSERT INTO genres (id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Триллер'),
       (4, 'Мультфильм'),
       (5, 'Документальный') ON CONFLICT (id) DO NOTHING;