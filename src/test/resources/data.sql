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
        (4, 'Детектив'),
        (5, 'Фантастика'),
        (6, 'Документальный');

INSERT INTO FILM_DIRECTOR
(NAME)
VALUES
    ('Director_1'),
    ('Director_2'),
    ('Director_3');

INSERT INTO USERS
    (NAME, LOGIN, BIRTHDAY, EMAIL)
VALUES
    ('tom', 'tomtom', '1980-12-5', 'tom@tom.com'),
    ('friend', 'pirate', '1930-5-7', 'yarr@tortu.ga'),
    ('saul', 'bettercallhim', '1987-4-8', 'saul@good.man');
INSERT INTO FILM
    (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING, DIRECTOR_ID)
VALUES
    ('Pulp Friction', 'about friction and pulp', '1993-5-22', 180, 4, 1),
    ('Titanic', 'underwater drama', '1991-3-15', 150, 3, 3),
    ('Star Wars: Episode I – The Phantom Menace', 'the first episode of the saga', '1999-7-29', 136, 2, 2),
    ('Double Impact', 'Two brothers separated by the violence. Now together in a mission of revenge', '1991-7-31', 118, 4, 3);

INSERT INTO FILM_GENRE
    (FILM_ID, GENRE_ID)
VALUES
    (1, 3),
    (2, 5),
    (3, 3);



