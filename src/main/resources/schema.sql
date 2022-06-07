create table IF NOT EXISTS FILM
(
    FILM_ID      INTEGER not null GENERATED BY DEFAULT AS IDENTITY (START WITH 1),
    NAME         CHARACTER VARYING,
    DESCRIPTION  CHARACTER VARYING,
    RELEASE_DATE TIMESTAMP,
    DURATION     INTEGER,
    RATING       INTEGER,
    constraint FILM_ID
    primary key (FILM_ID)
    );

create table IF NOT EXISTS GENRE
(
    GENRE_ID INTEGER not null,
    GENRE    CHARACTER VARYING,
    constraint GENRE_ID
    primary key (GENRE_ID)
    );

create table IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRE_PK
    primary key (FILM_ID, GENRE_ID),
    constraint "FILM_GENRE_film_FK"
    foreign key (FILM_ID) references FILM
    ON DELETE CASCADE,
    constraint "FILM_GENRE_genre_FK"
    foreign key (GENRE_ID) references GENRE
    );

create table IF NOT EXISTS USERS
(
    USER_ID  INTEGER not null GENERATED BY DEFAULT AS IDENTITY (START WITH 1),
    NAME     CHARACTER VARYING,
    LOGIN    CHARACTER VARYING,
    BIRTHDAY TIMESTAMP,
    EMAIL    CHARACTER VARYING,
    constraint TABLE_NAME_PK
    primary key (USER_ID)
    );

create table IF NOT EXISTS FILM_LIKE
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint FILM_LIKE_PK
    primary key (FILM_ID, USER_ID),
    constraint FILM_LIKE_FILM_FK
    foreign key (FILM_ID) references FILM
    ON DELETE CASCADE,
    constraint FILM_LIKE_USER_FK
    foreign key (USER_ID) references USERS
    ON DELETE CASCADE
    );

create table IF NOT EXISTS USER_FRIEND
(
    USER_ID           INTEGER not null,
    FRIEND_ID         INTEGER not null,
    FRIENDSHIP_STATUS CHARACTER VARYING,
    constraint USER_FRIEND_PK
    primary key (USER_ID, FRIEND_ID),
    constraint FRIEND_ID
    foreign key (USER_ID) references USERS,
    constraint USER_ID
    foreign key (USER_ID) references USERS
    );

create table IF NOT EXISTS FILM_RATING
(
    RATING_ID INTEGER not null,
    MPA       CHARACTER VARYING,
    constraint FILM_RATING_PK
    primary key (RATING_ID)
    );