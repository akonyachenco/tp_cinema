package tp.project.cinema.dto.Mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.model.Session;

@Mapper(componentModel = "spring", uses = {BookingMapping.class})
public interface SessionMapping {

    @Mapping(source = "session_id", target = "sessionId")
    @Mapping(source = "film.film_id", target = "filmId")
    @Mapping(source = "hall.hall_id", target = "hallId")
    @Mapping(source = "date_time", target = "dateTime")
    @Mapping(target = "filmTitle", expression = "java(entity.getFilm() != null ? entity.getFilm().getTitle() : null)")
    @Mapping(target = "hallName", expression = "java(entity.getHall() != null ? entity.getHall().getHall_name() : null)")
    @Mapping(target = "duration", expression = "java(entity.getFilm() != null ? entity.getFilm().getDuration() : 0)")
    @Mapping(target = "basePrice", expression = "java(entity.getHall() != null ? entity.getHall().getBase_price() : null)")
    SessionDto toDto(Session entity);

    @Mapping(target = "session_id", ignore = true)
    @Mapping(target = "booking_list", ignore = true)
    @Mapping(target = "film", ignore = true)
    @Mapping(target = "hall", ignore = true)
    @Mapping(source = "dateTime", target = "date_time")
    Session toEntity(SessionDto dto);
}