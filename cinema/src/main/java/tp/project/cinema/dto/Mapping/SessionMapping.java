package tp.project.cinema.dto.Mapping;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.model.Session;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {BookingMapping.class})
public interface SessionMapping {

    @Mapping(source = "sessionId", target = "sessionId")
    @Mapping(source = "film.filmId", target = "filmId")
    @Mapping(source = "hall.hallId", target = "hallId")
    @Mapping(source = "dateTime", target = "dateTime")
    @Mapping(target = "filmTitle", expression = "java(entity.getFilm() != null ? entity.getFilm().getTitle() : null)")
    @Mapping(target = "hallName", expression = "java(entity.getHall() != null ? entity.getHall().getHallName() : null)")
    @Mapping(target = "duration", expression = "java(entity.getFilm() != null ? (Integer) (int) entity.getFilm().getDuration() : 0)")
    @Mapping(target = "basePrice", expression = "java(entity.getHall() != null ? entity.getHall().getBasePrice().doubleValue() : null)")
    SessionDto toDto(Session entity);

    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "bookingList", ignore = true)
    @Mapping(target = "film", ignore = true)
    @Mapping(target = "hall", ignore = true)
    @Mapping(source = "dateTime", target = "dateTime")
    Session toEntity(SessionDto dto);


    @AfterMapping
    default void checkStatus(@MappingTarget SessionDto dto, Session session) {
        if(!session.getStatus().equals("Завершен") && !session.getStatus().equals("Отменен")) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sessionDateTime = session.getDateTime();
            LocalDateTime sessionEndTime = sessionDateTime.plusMinutes(session.getFilm().getDuration());

            if(sessionEndTime.isBefore(now)) {
                session.setStatus("Завершен");
                dto.setStatus("Завершен");
            }
            else if(sessionDateTime.isBefore(now) && sessionEndTime.isAfter(now)) {
                session.setStatus("Активен");
                dto.setStatus("Активен");
            }
        }
    }
}