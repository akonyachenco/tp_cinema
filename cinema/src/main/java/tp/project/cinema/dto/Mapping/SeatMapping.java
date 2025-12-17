package tp.project.cinema.dto.Mapping;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.model.Seat;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {TicketMapping.class})
public interface SeatMapping {

    @Mapping(source = "seat_id", target = "seatId")
    @Mapping(source = "row_number", target = "rowNumber")
    @Mapping(source = "seat_number", target = "seatNumber")
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

    @AfterMapping
    default void calculatePrice(@MappingTarget SeatDto dto, Seat entity) {
        if (entity.getSeat_type() != null && entity.getHall() != null
                && entity.getHall().getBase_price() != null
                && entity.getSeat_type().getPrice_multiplier() != null) {
            BigDecimal calculatedPrice = entity.getHall().getBase_price()
                    .multiply(entity.getSeat_type().getPrice_multiplier());
            dto.setPrice(calculatedPrice);
        }
    }
}