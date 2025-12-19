package tp.project.cinema.dto.Mapping;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tp.project.cinema.dto.DirectorDto;
import tp.project.cinema.model.Country;
import tp.project.cinema.model.Director;

@Mapper(componentModel = "spring", uses = {FilmMapping.class})
public interface DirectorMapping {

    @Mapping(target = "directorId", source = "directorId")
    @Mapping(target = "countryId", source = "country.countryName")
    DirectorDto toDto(Director director);

    @Mapping(target = "directorId", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "filmList", ignore = true)
    Director toEntity(DirectorDto directorDto);

    @BeforeMapping
    default void buildNameAndSurname(@MappingTarget DirectorDto directorDto, Director director) {
        directorDto.setDirectorNameAndSurname(director.getName() + " " + director.getSurname());
    }

    @BeforeMapping
    default void separateNameAndSurname(@MappingTarget Director director, DirectorDto directorDto) {
        director.setSurname(
            directorDto.getDirectorNameAndSurname().split(" ")[0]);
        director.setName(
                directorDto.getDirectorNameAndSurname().split(" ")[1]);
    }
}
