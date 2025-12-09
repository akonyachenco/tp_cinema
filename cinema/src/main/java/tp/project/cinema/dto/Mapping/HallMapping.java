package tp.project.cinema.dto.Mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tp.project.cinema.dto.HallDto;
import tp.project.cinema.model.Hall;

@Mapper(componentModel = "spring", uses = {SeatMapping.class, SessionMapping.class})
public interface HallMapping {

    @Mapping(source = "hall_id", target = "hallId")
    @Mapping(source = "hall_type.type_name", target = "hallType")
    HallDto toDto(Hall entity);

    @Mapping(target = "hall_id", ignore = true)
    @Mapping(target = "seat_list", ignore = true)
    @Mapping(target = "session_list" , ignore = true)
    @Mapping(target = "hall_type", ignore = true)
    Hall toEntity(HallDto dto);
}
