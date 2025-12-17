package tp.project.cinema.dto.Mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.model.Session;

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
}