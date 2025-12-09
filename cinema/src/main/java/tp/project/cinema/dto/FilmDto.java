package tp.project.cinema.dto;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmDto {
    private Long filmId;
    private String description;
    private String title;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private Short duration;
    private Integer directorId;
    private Short countryId;
    private String ageRating;
    private List<String> genres;
    private List<SessionDto> sessionList;
}
