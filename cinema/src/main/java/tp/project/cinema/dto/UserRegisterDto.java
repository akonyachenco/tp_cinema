package tp.project.cinema.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegisterDto {
    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 40)
    private String name;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2, max = 40)
    private String surname;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email адрес")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;
}
