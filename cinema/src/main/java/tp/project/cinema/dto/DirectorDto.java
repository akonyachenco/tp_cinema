package tp.project.cinema.dto;

import lombok.Data;

import java.util.List;

@Data
public class DirectorDto {
    private int directorId;
    private String directorNameAndSurname;
    private Short countryId;
    private List<FilmDto> filmList;
}
