package tp.project.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SessionDto {
    private Integer sessionId;
    private String status;
    private LocalDateTime dateTime;

    @NotNull(message = "ID фильма обязателен")
    private Long filmId;

    @NotNull(message = "ID зала обязателен")
    private Short hallId;

    private List<BookingDto> bookingList;

    private String filmTitle;
    private String hallName;
    private Integer duration;
    private Double basePrice;
    private Map<String, Object> seatInfo;
}