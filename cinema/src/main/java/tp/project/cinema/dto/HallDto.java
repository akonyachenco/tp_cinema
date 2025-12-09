package tp.project.cinema.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HallDto {
    private Short hallId;
    private String status;
    private BigDecimal basePrice;
    private Short rows_count;
    private Short seatsPerRow;
    private String hallName;
    private String hallType;
    private List<SessionDto> sessionList;
    private List<SeatDto> seatList;
}
