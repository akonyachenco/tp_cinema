package tp.project.cinema.dto.Mapping;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.model.Seat;

@Mapper(componentModel = "spring", uses = {TicketMapping.class})
public interface SeatMapping {

    @Mapping(source = "seat_id", target = "seatId")
    @Mapping(source = "seat_type.type_name", target = "seatType")
    @Mapping(source = "seat_type.price_multiplier", target = "priceMultiplier")
    @Mapping(source = "hall.base_price", target = "basePrice")
    @Mapping(source = "hall.hall_name", target = "hallName")
    @Mapping(source = "hall.hall_id", target = "hallId")
    SeatDto toDto(Seat entity);

    @Mapping(target = "seat_id", ignore = true)
    @Mapping(target = "ticket_list", ignore = true)
    @Mapping(target = "seat_type", ignore = true)
    @Mapping(target = "hall", ignore = true)
    Seat toEntity(SeatDto dto);

}
