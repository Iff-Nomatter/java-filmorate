MERGE INTO FILM_RATING
    (RATING_ID, MPA)
    VALUES
        (1, 'G' ),
        (2, 'PG'),
        (3, 'PG-13'),
        (4, 'R'),
        (5, 'NC-17');

MERGE INTO GENRE
    (GENRE_ID, GENRE)
    VALUES
        (1, 'Комедия'),
        (2, 'Драма'),
        (3, 'Мультфильм'),
        (4, 'Detective'),
        (5, 'Drama'),
        (6, 'Historical movie');

INSERT INTO USERS
    (NAME, LOGIN, BIRTHDAY, EMAIL)
VALUES
    ('tom', 'tomtom', '1980-12-5', 'tom@tom.com'),
    ('friend', 'pirate', '1930-5-7', 'yarr@tortu.ga'),
    ('saul', 'bettercallhim', '1987-4-8', 'saul@good.man');

INSERT INTO FILM
    (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING)
VALUES
    ('Pulp Friction', 'about friction and pulp', '1993-5-22', 180, 4),
    ('Titanic', 'underwater drama', '1991-3-15', 150, 3);