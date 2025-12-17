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
    @Mapping(source = "booking_time", target = "bookingTime")
    @Mapping(source = "total_cost", target = "totalCost")
    @Mapping(source = "user.user_id", target = "userId")
    @Mapping(source = "session.session_id", target = "sessionId")
    @Mapping(source = "booking_status.status_name", target = "status")
    @Mapping(target = "userName", expression = "java(entity.getUser() != null ? entity.getUser().getName() + \" \" + entity.getUser().getSurname() : null)")
    @Mapping(target = "userEmail", expression = "java(entity.getUser() != null ? entity.getUser().getEmail() : null)")
    @Mapping(target = "filmTitle", expression = "java(entity.getSession() != null && entity.getSession().getFilm() != null ? entity.getSession().getFilm().getTitle() : null)")
    @Mapping(target = "hallName", expression = "java(entity.getSession() != null && entity.getSession().getHall() != null ? entity.getSession().getHall().getHall_name() : null)")
    @Mapping(target = "sessionDateTime", expression = "java(entity.getSession() != null ? entity.getSession().getDate_time() : null)")
    @Mapping(target = "ticketCount", expression = "java(entity.getTicket_list() != null ? entity.getTicket_list().size() : 0)")
    BookingDto toDto(Booking entity);

    @Mapping(target = "booking_id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "booking_status", ignore = true)
    @Mapping(target = "ticket_list", ignore = true)
    Booking toEntity(BookingDto dto);

    @AfterMapping
    default void calculateTotalCost(@MappingTarget BookingDto dto, Booking entity) {
        if (dto.getTotalCost() == null && entity.getTicket_list() != null && !entity.getTicket_list().isEmpty()) {
            BigDecimal totalCost = BigDecimal.ZERO;
            for (Ticket ticket : entity.getTicket_list()) {
                if (ticket.getPrice() != null) {
                    totalCost = totalCost.add(ticket.getPrice());
                }
            }
            dto.setTotalCost(totalCost);
        }
    }
}