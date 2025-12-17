package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.HallDto;
import tp.project.cinema.model.Hall;

@Mapper(componentModel = "spring", uses = {SeatMapping.class, SessionMapping.class})
public interface HallMapping {

    @Mapping(source = "hallId", target = "hallId")
    @Mapping(source = "hallType.typeName", target = "hallType")
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "rowsCount", target = "rowsCount")
    @Mapping(source = "seatsPerRow", target = "seatsPerRow")
    @Mapping(source = "hallName", target = "hallName")
    HallDto toDto(Hall entity);

    @Mapping(target = "hallId", ignore = true)
    @Mapping(target = "seatList", ignore = true)
    @Mapping(target = "sessionList", ignore = true)
    @Mapping(target = "hallType", ignore = true)
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "rowsCount", target = "rowsCount")
    @Mapping(source = "seatsPerRow", target = "seatsPerRow")
    @Mapping(source = "hallName", target = "hallName")
    Hall toEntity(HallDto dto);
}