package ru.yandex.practicum.filmorate.model;

import lombok.Data;

//родительский класс для объектов Film и User для обращения к их id
//внутри абстрактного класса Controller
@Data
public class IdHolder {
    private int id;
}
