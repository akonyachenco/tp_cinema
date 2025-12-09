package tp.project.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SessionDto {
    private Integer sessionId;
    private String status;
    private LocalDateTime dateTime;
    @NotNull
    private Long filmId;
    @NotNull
    private Short hallId;
    private List<BookingDto> bookingList;
}
