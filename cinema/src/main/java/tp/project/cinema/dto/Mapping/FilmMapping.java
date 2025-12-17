package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.model.Film;
import tp.project.cinema.model.FilmGenre;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {SessionMapping.class})
public interface FilmMapping {

    @Mapping(source = "filmId", target = "filmId")
    @Mapping(source = "director.directorId", target = "directorId")
    @Mapping(source = "country.countryId", target = "countryId")
    @Mapping(source = "ageRating.ratingValue", target = "ageRating")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "trailerUrl", target = "trailerUrl")
    @Mapping(target = "duration", expression = "java(entity.getDuration() != 0 ? (Integer) (int) entity.getDuration() : null)")
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "directorName", expression = "java(entity.getDirector() != null ? entity.getDirector().getName() + \" \" + entity.getDirector().getSurname() : null)")
    @Mapping(target = "countryName", expression = "java(entity.getCountry() != null ? entity.getCountry().getCountryName() : null)")
    FilmDto toDto(Film entity);

    @Mapping(target = "filmId", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "ageRating", ignore = true)
    @Mapping(target = "sessionList", ignore = true)
    @Mapping(target = "filmGenreList", ignore = true)
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "trailerUrl", target = "trailerUrl")
    @Mapping(target = "duration", ignore = true)
    Film toEntity(FilmDto dto);

    @AfterMapping
    default void fillGenres(@MappingTarget FilmDto dto, Film entity) {
        if (entity.getFilmGenreList() != null && !entity.getFilmGenreList().isEmpty()) {
            List<String> genres = new ArrayList<>();
            for (FilmGenre filmGenre : entity.getFilmGenreList()) {
                if (filmGenre.getGenre() != null && filmGenre.getGenre().getGenreName() != null) {
                    genres.add(filmGenre.getGenre().getGenreName());
                }
            }
            dto.setGenres(genres);
        }
    }

    @AfterMapping
    default void setSessionInfo(@MappingTarget FilmDto dto, Film entity) {
        if (entity.getSessionList() != null) {
            dto.setSessionCount(entity.getSessionList().size());
        }
    }

    @BeforeMapping
    default void setDuration(@MappingTarget Film entity, FilmDto dto) {
        if (dto.getDuration() != null) {
            entity.setDuration(dto.getDuration());
        } else {
            entity.setDuration((short) 0);
        }
    }
}