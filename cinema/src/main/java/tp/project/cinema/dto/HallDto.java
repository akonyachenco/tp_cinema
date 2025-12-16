package tp.project.cinema.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HallDto {
    private Short hallId;

    @NotNull(message = "Статус зала обязателен")
    private String status;

    @NotNull(message = "Базовая цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private BigDecimal basePrice;

    @NotNull(message = "Количество рядов обязательно")
    @Min(value = 1, message = "Количество рядов должно быть не менее 1")
    private Short rows_count;

    @NotNull(message = "Количество мест в ряду обязательно")
    @Min(value = 1, message = "Количество мест в ряду должно быть не менее 1")
    private Short seatsPerRow;

    @NotNull(message = "Название зала обязательно")
    @Size(min = 2, max = 50, message = "Название зала должно быть от 2 до 50 символов")
    private String hallName;

    @NotNull(message = "Тип зала обязателен")
    private String hallType;

    private List<SessionDto> sessionList;
    private List<SeatDto> seatList;
}