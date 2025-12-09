package tp.project.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingDto {

    private Long bookingId;
    private LocalDateTime bookingTime;
    private BigDecimal totalCost;
    @NotNull
    private Long userId;
    @NotNull
    private Long sessionId;
    @NotNull
    private String status;
    private List<TicketDto> ticketList;
}
