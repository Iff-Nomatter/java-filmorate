package ru.yandex.practicum.filmorate.validators;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

//управление списком нарушений валидации
@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

    private final List<Violation> violations;

}

