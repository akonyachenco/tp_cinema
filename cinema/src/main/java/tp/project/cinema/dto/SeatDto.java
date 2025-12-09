package tp.project.cinema.dto;

import lombok.Data;
import tp.project.cinema.model.Seat;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SeatDto {
    private Integer seatId;
    private Short rowNumber;
    private Short seatNumber;
    private String seatType;
    private BigDecimal priceMultiplier;
    private Short hallId;
    private BigDecimal basePrice;
    private String hallName;
    private List<TicketDto> ticketList;
}
