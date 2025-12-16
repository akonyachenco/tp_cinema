package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.HallDto;
import tp.project.cinema.model.Hall;

@Mapper(componentModel = "spring", uses = {SeatMapping.class, SessionMapping.class})
public interface HallMapping {

    @Mapping(source = "hall_id", target = "hallId")
    @Mapping(source = "hall_type.type_name", target = "hallType")
    @Mapping(source = "base_price", target = "basePrice")
    @Mapping(source = "rows_count", target = "rows_count")
    @Mapping(source = "seats_per_row", target = "seatsPerRow")
    @Mapping(source = "hall_name", target = "hallName")
    HallDto toDto(Hall entity);

    @Mapping(target = "hall_id", ignore = true)
    @Mapping(target = "seat_list", ignore = true)
    @Mapping(target = "session_list", ignore = true)
    @Mapping(target = "hall_type", ignore = true)
    @Mapping(source = "basePrice", target = "base_price")
    @Mapping(source = "rows_count", target = "rows_count")
    @Mapping(source = "seatsPerRow", target = "seats_per_row")
    @Mapping(source = "hallName", target = "hall_name")
    Hall toEntity(HallDto dto);
}