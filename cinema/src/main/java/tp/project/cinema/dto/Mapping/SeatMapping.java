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

    @Mapping(source = "seatId", target = "seatId")
    @Mapping(source = "rowNumber", target = "rowNumber")
    @Mapping(source = "seatNumber", target = "seatNumber")
    @Mapping(source = "seatType.typeName", target = "seatType")
    @Mapping(source = "seatType.priceMultiplier", target = "priceMultiplier")
    @Mapping(source = "hall.basePrice", target = "basePrice")
    @Mapping(source = "hall.hallName", target = "hallName")
    @Mapping(source = "hall.hallId", target = "hallId")
    SeatDto toDto(Seat entity);

    @Mapping(target = "seatId", ignore = true)
    @Mapping(target = "ticketList", ignore = true)
    @Mapping(target = "seatType", ignore = true)
    @Mapping(target = "hall", ignore = true)
    Seat toEntity(SeatDto dto);

    @AfterMapping
    default void calculatePrice(@MappingTarget SeatDto dto, Seat entity) {
        if (entity.getSeatType() != null && entity.getHall() != null
                && entity.getHall().getBasePrice() != null
                && entity.getSeatType().getPriceMultiplier() != null) {
            BigDecimal calculatedPrice = entity.getHall().getBasePrice()
                    .multiply(entity.getSeatType().getPriceMultiplier());
            dto.setPrice(calculatedPrice);
        }
    }
}