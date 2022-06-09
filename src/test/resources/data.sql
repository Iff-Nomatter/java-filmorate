MERGE INTO FILM_RATING
                            (RATING_ID, MPA)
VALUES
                            (1, 'G' ),
                            (2, 'PG'),
                            (3, 'PG-13'),
                            (4, 'R'),
                            (5, 'NC-17');

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
    ('Titanic', 'underwater drama', '1991-3-15', 150, 3, 2),
    ('Film_3', 'Description_3', '2007-9-3', 420, 3, 1);

INSERT INTO FILM_DIRECTOR
    (NAME)
VALUES
    ('Director_1'),
    ('Director_2'),
    ('Director_3');



