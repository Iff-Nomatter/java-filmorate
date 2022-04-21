package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends IdHolder {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "^\\S+$", message = "не должен содержать пробелы")
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
}
