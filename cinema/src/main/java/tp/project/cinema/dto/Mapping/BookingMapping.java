package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.BookingDto;
import tp.project.cinema.model.Booking;
import tp.project.cinema.model.BookingStatus;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {TicketMapping.class})
public interface BookingMapping {

    @Mapping(source = "bookingId", target = "bookingId")
    @Mapping(source = "bookingTime", target = "bookingTime")
    @Mapping(source = "totalCost", target = "totalCost")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "session.sessionId", target = "sessionId")
    @Mapping(source = "bookingStatus.statusName", target = "status")
    @Mapping(target = "userName", expression = "java(entity.getUser() != null ? entity.getUser().getName() + \" \" + entity.getUser().getSurname() : null)")
    @Mapping(target = "userEmail", expression = "java(entity.getUser() != null ? entity.getUser().getEmail() : null)")
    @Mapping(target = "filmTitle", expression = "java(entity.getSession() != null && entity.getSession().getFilm() != null ? entity.getSession().getFilm().getTitle() : null)")
    @Mapping(target = "hallName", expression = "java(entity.getSession() != null && entity.getSession().getHall() != null ? entity.getSession().getHall().getHallName() : null)")
    @Mapping(target = "sessionDateTime", expression = "java(entity.getSession() != null ? entity.getSession().getDateTime() : null)")
    @Mapping(target = "ticketCount", expression = "java(entity.getTicketList() != null ? entity.getTicketList().size() : 0)")
    BookingDto toDto(Booking entity);

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    @Mapping(target = "ticketList", ignore = true)
    Booking toEntity(BookingDto dto);


    @BeforeMapping
    default void calculateTotalCost(@MappingTarget BookingDto bookingDto, Booking entity) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Ticket ticket : entity.getTicketList()) {
            if (ticket.getPrice() != null) {
                totalCost = totalCost.add(ticket.getPrice());
            }
        }
        entity.setTotalCost(totalCost);
        bookingDto.setTotalCost(totalCost);
    }

    @AfterMapping
    default void checkSessionStatus(@MappingTarget BookingDto bookingDto, Booking entity) {
        if (!bookingDto.getStatus().equals("Отмена") && !bookingDto.getStatus().equals("Завершено")) {
            String status = entity.getSession().getStatus();
            switch (status) {
                case "Отменен":
                    bookingDto.setStatus("Отмена");
                    break;
                case "Завершен":
                    bookingDto.setStatus("Завершено");
                    break;
            }
        }
    }
}