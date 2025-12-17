package tp.project.cinema.dto;

import lombok.Data;
import lombok.Setter;

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

    private String status; // "AVAILABLE", "BOOKED", "SELECTED"
    private BigDecimal price; // Рассчитанная цена

    @Setter
    private Boolean isSelected; // Для UI

    public Boolean getIsSelected() {
        return isSelected != null ? isSelected : false;
    }
}