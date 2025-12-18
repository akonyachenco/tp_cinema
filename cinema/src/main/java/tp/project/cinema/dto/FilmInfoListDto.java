package tp.project.cinema.dto;

import lombok.Data;
import tp.project.cinema.model.Country;
import tp.project.cinema.model.Director;

import java.util.List;

@Data
public class FilmInfoListDto {
    private List<Director> directors;
    private List<Country> countries;
}
