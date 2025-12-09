package tp.project.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDto {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email адрес")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}
