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

MERGE INTO FILM_DIRECTOR
                            (DIRECTOR_ID, NAME)
VALUES
    (1, 'Director_1'),
    (2, 'Director_2'),
    (3, 'Director_3');
