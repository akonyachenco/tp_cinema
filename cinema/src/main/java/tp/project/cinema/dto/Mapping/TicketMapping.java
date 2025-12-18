package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicketMapping {

    @Mapping(source = "ticketId", target = "ticketId")
    @Mapping(source = "creationDate", target = "creationDate")
    @Mapping(source = "ticketCode", target = "ticketCode")
    @Mapping(source = "seat.seatId", target = "seatId")
    @Mapping(source = "booking.bookingId", target = "bookingId")
    @Mapping(target = "seatType", expression = "java(entity.getSeat() != null && entity.getSeat().getSeatType() != null ? entity.getSeat().getSeatType().getTypeName() : null)")
    @Mapping(target = "rowNumber", expression = "java(entity.getSeat() != null ? entity.getSeat().getRowNumber() : null)")
    @Mapping(target = "seatNumber", expression = "java(entity.getSeat() != null ? entity.getSeat().getSeatNumber() : null)")
    @Mapping(target = "hallName", expression = "java(entity.getSeat() != null && entity.getSeat().getHall() != null ? entity.getSeat().getHall().getHallName() : null)")
    @Mapping(target = "filmTitle", expression = "java(entity.getBooking() != null && entity.getBooking().getSession() != null && entity.getBooking().getSession().getFilm() != null ? entity.getBooking().getSession().getFilm().getTitle() : null)")
    @Mapping(target = "sessionDateTime", expression = "java(entity.getBooking() != null && entity.getBooking().getSession() != null ? entity.getBooking().getSession().getDateTime() : null)")
    @Mapping(target = "status", expression = "java(entity.getBooking() != null && entity.getBooking().getBookingStatus() != null ? entity.getBooking().getBookingStatus().getStatusName() : null)")
    TicketDto toDto(Ticket entity);

    @Mapping(target = "ticketId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "ticketCode", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Ticket toEntity(TicketDto dto);

    @BeforeMapping
    default void generateTicketCode(@MappingTarget Ticket entity) {
        if (entity.getTicketCode() == null || entity.getTicketCode().isEmpty()) {
            String uuidPart = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 9)
                    .toUpperCase();

            String timePart = String.valueOf(System.currentTimeMillis() % 100000);

            entity.setTicketCode(String.format("TK-%s-%s", timePart, uuidPart));
        }
    }

    @AfterMapping
    default void calculatePrice(@MappingTarget Ticket ticket) {
        ticket.setPrice(
                ticket.getSeat().getHall().getBasePrice()
                        .multiply(ticket.getSeat().getSeatType().getPriceMultiplier())
        );
    }
}