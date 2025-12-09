package tp.project.cinema.dto.Mapping;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tp.project.cinema.dto.BookingDto;
import tp.project.cinema.model.Booking;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {TicketMapping.class})
public interface BookingMapping {

    @Mapping(source = "booking_id", target = "bookingId")
    @Mapping(source = "user.user_id", target = "userId")
    @Mapping(source = "session.session_id", target = "sessionId")
    @Mapping(source = "booking_status.status_name", target = "status")
    @Mapping(target = "totalCost", ignore = true)
    BookingDto toDto(Booking entity);

    @Mapping(target = "booking_id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "booking_status", ignore = true)
    @Mapping(target = "ticket_list", ignore = true)
    Booking toEntity(BookingDto dto);

    @AfterMapping
    default void calculateTotalCost(@MappingTarget BookingDto dto, Booking entity) {

        if(entity.getTicket_list() != null && !entity.getTicket_list().isEmpty()) {
            BigDecimal totalCost = BigDecimal.ZERO;
            for (Ticket  ticket : entity.getTicket_list()) {
                totalCost = totalCost.add(ticket.getPrice());
            }
            dto.setTotalCost(totalCost);
        }
    }
}
