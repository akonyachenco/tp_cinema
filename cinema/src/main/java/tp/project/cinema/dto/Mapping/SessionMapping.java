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
    SessionDto toDto(Session entity);

    @Mapping(target = "session_id", ignore = true)
    @Mapping(target = "booking_list", ignore = true)
    @Mapping(target = "film", ignore = true)
    @Mapping(target = "hall", ignore = true)
    Session toEntity(SessionDto dto);
}
