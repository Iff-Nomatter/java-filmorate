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
                            (1, 'Action'),
                            (2, 'Western'),
                            (3, 'Gangster movie'),
                            (4, 'Detective'),
                            (5, 'Drama'),
                            (6, 'Historical movie'),
                            (7, 'Comedy'),
                            (8, 'Melodrama'),
                            (9, 'Musical movie'),
                            (10, 'Noire'),
                            (11, 'Political movie'),
                            (12, 'Adventure movie'),
                            (13, 'Tale'),
                            (14, 'Tragedy'),
                            (15, 'Tragic comedy'),
                            (16, 'Thriller'),
                            (17, 'Sci-fi  movie'),
                            (18, 'Horror'),
                            (19, 'Catastrophe movie'),
                            (20, 'Fantasy');
