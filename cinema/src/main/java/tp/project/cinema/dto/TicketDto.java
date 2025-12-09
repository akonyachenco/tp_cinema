package tp.project.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tp.project.cinema.model.HallType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TicketDto {
    private Long ticketId;
    private LocalDateTime creationDate;
    private BigDecimal price;
    private String ticketCode;
    @NotNull
    private Integer seatId;
    @NotNull
    private Long bookingId;
}
