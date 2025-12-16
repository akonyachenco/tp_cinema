package tp.project.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingDto {
    private Long bookingId;
    private LocalDateTime bookingTime;
    private BigDecimal totalCost;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @NotNull(message = "ID сеанса обязателен")
    private Long sessionId;

    @NotNull(message = "Статус обязателен")
    private String status;

    // Добавлено для создания бронирования с местами
    private List<TicketDto> ticketList;
}