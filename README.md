# Filmorate
Сервис для оценки фильмов и взаимодействия пользователей.  
Ниже представлена актуальная схема базы данных и описание её структуры.

![Filmorate ER Diagram](docs/filmorate-er-diagram.png)

---

## Описание ER-диаграммы

Диаграмма описывает модель данных приложения Filmorate.  
- значения атомарны,
- отсутствует дублирование данных,
- связи оформлены через внешние ключи,
- многие-ко-многим реализованы через промежуточные таблицы.

---

# ️ Структура базы данных

## Таблица `users` — пользователи
Хранит основную информацию:

| Поле      | Тип            | Описание |
|-----------|----------------|----------|
| `id`      | int PK         | уникальный идентификатор |
| `email`   | varchar(255)   | уникальный email |
| `login`   | varchar(255)   | уникальный логин |
| `name`    | varchar(255)   | отображаемое имя |
| `birthday`| date           | дата рождения |

---

## Таблица `films` — фильмы

| Поле          | Тип            | Описание |
|---------------|----------------|----------|
| `id`          | int PK         | уникальный идентификатор |
| `name`        | varchar        | название |
| `description` | varchar(200)   | описание (ограничено 200 символами) |
| `release_date`| date           | дата релиза |
| `duration`    | int            | продолжительность |
| `mpa_id`      | int FK         | ссылка на MPA-рейтинг |

---

## Таблица `mpa_ratings` — рейтинги MPA (справочник)

Содержит фиксированный набор значений:

| id | name     |
|----|----------|
| 1  | G        |
| 2  | PG       |
| 3  | PG-13    |
| 4  | R        |
| 5  | NC-17    |

---

## Таблица `genres` — жанры (справочник)

Список жанров: Комедия, Драма, Триллер, Мультфильм, Документальный, Боевик и др.

---

## Таблица `film_genres` — связь фильм–жанр

Реализует отношение **многие-ко-многим**:

| Поле       | Тип     | Описание |
|------------|---------|----------|
| `film_id`  | int FK  | фильм |
| `genre_id` | int FK  | жанр |

Составной PK гарантирует уникальность пары `(film_id, genre_id)`.

---

## Таблица `film_likes` — лайки фильмов

Отражает связь «пользователь лайкнул фильм»:

| Поле       | Тип     |
|------------|---------|
| `film_id`  | int FK  |
| `user_id`  | int FK  |

Пара `(film_id, user_id)` уникальна → нельзя лайкнуть дважды.

---

## Таблица `friendships` — дружба (односторонняя)

Запись означает: user_id → friend_id

| Поле        | Тип   | Описание |
|-------------|--------|----------|
| `user_id`   | int FK | пользователь |
| `friend_id` | int FK | его друг |

---

# Основные SQL-запросы

---

## Получить всех пользователей
```sql
SELECT * FROM users;
```

## Получить все фильмы с рейтингами MPA
```sql
SELECT f.id,
f.name,
f.description,
f.release_date,
f.duration,
m.name AS mpa_rating
FROM films f
JOIN mpa_ratings m ON f.mpa_id = m.id;
```

## Топ N популярных фильмов (по количеству лайков)
```sql
SELECT f.id,
f.name,
COUNT(fl.user_id) AS likes_count
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC, f.id
LIMIT :count;
```

## Получить друзей пользователя (односторонняя дружба)
```sql
SELECT u.*
FROM friendships fs
JOIN users u ON fs.friend_id = u.id
WHERE fs.user_id = :userId;
```

## Поиск общих друзей двух пользователей
```sql
SELECT u.*
FROM friendships f1
JOIN friendships f2 ON f1.friend_id = f2.friend_id
JOIN users u ON u.id = f1.friend_id
WHERE f1.user_id = :userId
AND f2.user_id = :otherId;
```