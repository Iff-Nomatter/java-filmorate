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
                            (3, 'Gangster movie'),
                            (4, 'Detective'),
                            (5, 'Drama'),
                            (6, 'Historical movie');