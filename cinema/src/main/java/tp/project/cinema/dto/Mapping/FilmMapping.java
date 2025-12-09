package tp.project.cinema.dto.Mapping;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
    @Mapping(target = "genres", ignore = true)
    FilmDto toDto(Film entity);

    @Mapping(target = "film_id", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "age_rating", ignore = true)
    @Mapping(target = "session_list", ignore = true)
    @Mapping(target = "film_genre_list", ignore = true)
    Film toEntity(FilmDto dto);



    @AfterMapping
    default void fillGenres(@MappingTarget FilmDto dto, Film entity) {
        List<String> genres = new ArrayList<String>();
        for (FilmGenre filmGenre : entity.getFilm_genre_list()) {

            genres.add(filmGenre.getGenre().getGenre_name());
        }
        dto.setGenres(genres);
    }
}
