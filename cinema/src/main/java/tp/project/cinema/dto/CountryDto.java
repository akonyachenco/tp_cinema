package tp.project.cinema.dto;

import lombok.Data;

import java.util.List;

@Data
public class CountryDto {
    private Short countryId;
    private String countryName;
    private List<DirectorDto> directorList;
    private List<FilmDto> filmList;
}
