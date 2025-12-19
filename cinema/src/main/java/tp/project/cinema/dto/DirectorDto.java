package tp.project.cinema.dto;

import lombok.Data;


import java.time.LocalDate;
import java.util.List;

@Data
public class DirectorDto {
    private Integer directorId;
    private String directorNameAndSurname;
    private LocalDate birthDate;
    private Short countryId;
    private List<FilmDto> filmList;
}
