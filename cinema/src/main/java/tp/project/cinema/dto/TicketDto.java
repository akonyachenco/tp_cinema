package tp.project.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TicketDto {
    private Long ticketId;
    private LocalDateTime creationDate;
    private BigDecimal price;
    private String ticketCode;

    @NotNull(message = "ID места обязателен")
    private Integer seatId;

    @NotNull(message = "ID бронирования обязателен")
    private Long bookingId;

    private String seatType;
    private Short rowNumber;
    private Short seatNumber;
    private String hallName;
    private String filmTitle;
    private LocalDateTime sessionDateTime;
    private String status;
}