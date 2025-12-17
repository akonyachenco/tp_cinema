package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.model.Film;
import tp.project.cinema.model.FilmGenre;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {SessionMapping.class})
public interface FilmMapping {

    @Mapping(source = "film_id", target = "filmId")
    @Mapping(source = "director.director_id", target = "directorId")
    @Mapping(source = "country.country_id", target = "countryId")
    @Mapping(source = "age_rating.rating_value", target = "ageRating")
    @Mapping(source = "release_date", target = "releaseDate")
    @Mapping(source = "poster_url", target = "posterUrl")
    @Mapping(source = "trailer_url", target = "trailerUrl")
    @Mapping(target = "duration", expression = "java(entity.getDuration() != 0 ? (Integer) (int) entity.getDuration() : null)")
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "directorName", expression = "java(entity.getDirector() != null ? entity.getDirector().getName() + \" \" + entity.getDirector().getSurname() : null)")
    @Mapping(target = "countryName", expression = "java(entity.getCountry() != null ? entity.getCountry().getCountry_name() : null)")
    FilmDto toDto(Film entity);

    @Mapping(target = "film_id", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "age_rating", ignore = true)
    @Mapping(target = "session_list", ignore = true)
    @Mapping(target = "film_genre_list", ignore = true)
    @Mapping(source = "releaseDate", target = "release_date")
    @Mapping(source = "posterUrl", target = "poster_url")
    @Mapping(source = "trailerUrl", target = "trailer_url")
    @Mapping(target = "duration", ignore = true)
    Film toEntity(FilmDto dto);

    @AfterMapping
    default void fillGenres(@MappingTarget FilmDto dto, Film entity) {
        if (entity.getFilm_genre_list() != null && !entity.getFilm_genre_list().isEmpty()) {
            List<String> genres = new ArrayList<>();
            for (FilmGenre filmGenre : entity.getFilm_genre_list()) {
                if (filmGenre.getGenre() != null && filmGenre.getGenre().getGenre_name() != null) {
                    genres.add(filmGenre.getGenre().getGenre_name());
                }
            }
            dto.setGenres(genres);
        }
    }

    @AfterMapping
    default void setSessionInfo(@MappingTarget FilmDto dto, Film entity) {
        if (entity.getSession_list() != null) {
            dto.setSessionCount(entity.getSession_list().size());
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