package ru.yandex.practicum.filmorate.validators;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//объект, описывающий нарушение валидации
@Getter
@RequiredArgsConstructor
public class Violation {

    private final String fieldName;
    private final String message;

}
