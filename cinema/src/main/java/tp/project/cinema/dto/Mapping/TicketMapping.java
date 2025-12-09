package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicketMapping {

    @Mapping(source = "ticket_id", target = "ticketId")
    @Mapping(target = "ticketCode", ignore = true)
    @Mapping(source = "seat.seat_id", target = "seatId")
    @Mapping(source = "booking.booking_id", target = "bookingId")
    TicketDto toDto(Ticket entity);

    @Mapping(target = "ticket_id", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Ticket toEntity(TicketDto dto);

    @BeforeMapping
    default void generateTicketCode(TicketDto dto, @MappingTarget Ticket entity) {

        String uuidPart = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 9)
                .toUpperCase();

        String timePart = String.valueOf(System.currentTimeMillis() % 100000);

        entity.setTicket_code(String.format("TK-%s-%s", timePart, uuidPart));
    }

}
