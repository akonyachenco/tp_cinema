package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicketMapping {

    @Mapping(source = "ticket_id", target = "ticketId")
    @Mapping(source = "creation_date", target = "creationDate")
    @Mapping(source = "ticket_code", target = "ticketCode")
    @Mapping(source = "seat.seat_id", target = "seatId")
    @Mapping(source = "booking.booking_id", target = "bookingId")
    @Mapping(target = "seatType", expression = "java(entity.getSeat() != null && entity.getSeat().getSeat_type() != null ? entity.getSeat().getSeat_type().getType_name() : null)")
    @Mapping(target = "rowNumber", expression = "java(entity.getSeat() != null ? entity.getSeat().getRow_number() : null)")
    @Mapping(target = "seatNumber", expression = "java(entity.getSeat() != null ? entity.getSeat().getSeat_number() : null)")
    @Mapping(target = "hallName", expression = "java(entity.getSeat() != null && entity.getSeat().getHall() != null ? entity.getSeat().getHall().getHall_name() : null)")
    @Mapping(target = "filmTitle", expression = "java(entity.getBooking() != null && entity.getBooking().getSession() != null && entity.getBooking().getSession().getFilm() != null ? entity.getBooking().getSession().getFilm().getTitle() : null)")
    @Mapping(target = "sessionDateTime", expression = "java(entity.getBooking() != null && entity.getBooking().getSession() != null ? entity.getBooking().getSession().getDate_time() : null)")
    @Mapping(target = "status", expression = "java(entity.getBooking() != null && entity.getBooking().getBooking_status() != null ? entity.getBooking().getBooking_status().getStatus_name() : null)")
    TicketDto toDto(Ticket entity);

    @Mapping(target = "ticket_id", ignore = true)
    @Mapping(target = "creation_date", ignore = true)
    @Mapping(target = "ticket_code", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Ticket toEntity(TicketDto dto);

    @BeforeMapping
    default void generateTicketCode(@MappingTarget Ticket entity) {
        if (entity.getTicket_code() == null || entity.getTicket_code().isEmpty()) {
            String uuidPart = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 9)
                    .toUpperCase();

            String timePart = String.valueOf(System.currentTimeMillis() % 100000);

            entity.setTicket_code(String.format("TK-%s-%s", timePart, uuidPart));
        }
    }

    @AfterMapping
    default void calculatePrice(@MappingTarget TicketDto dto, Ticket entity) {
        // Если цена не установлена, рассчитываем ее на основе места
        if (dto.getPrice() == null && entity.getSeat() != null) {
            BigDecimal basePrice = entity.getSeat().getHall().getBase_price();
            BigDecimal multiplier = entity.getSeat().getSeat_type().getPrice_multiplier();
            if (basePrice != null && multiplier != null) {
                dto.setPrice(basePrice.multiply(multiplier));
            }
        }
    }
}