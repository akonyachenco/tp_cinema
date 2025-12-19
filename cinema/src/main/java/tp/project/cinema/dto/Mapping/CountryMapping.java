package tp.project.cinema.dto.Mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tp.project.cinema.dto.CountryDto;
import tp.project.cinema.model.Country;

@Mapper(componentModel = "spring", uses = {DirectorMapping.class, FilmMapping.class})
public interface CountryMapping {

    @Mapping(target = "countryId", source = "countryId")
    CountryDto toDto(Country country);

    @Mapping(target = "countryId", ignore = true)
    @Mapping(target = "directorList", ignore = true)
    @Mapping(target = "filmList", ignore = true)
    Country toEntity(CountryDto countryDto);
}
