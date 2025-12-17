package tp.project.cinema.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmDto {
    private Long filmId;

    @NotBlank(message = "Описание обязательно")
    @Size(min = 10, max = 2000, message = "Описание должно быть от 10 до 2000 символов")
    private String description;

    @NotBlank(message = "Название обязательно")
    @Size(min = 1, max = 100, message = "Название должно быть от 1 до 100 символов")
    private String title;

    @NotNull(message = "Дата выхода обязательна")
    private LocalDate releaseDate;

    @URL(message = "Неверный URL постера")
    private String posterUrl;

    @URL(message = "Неверный URL трейлера")
    private String trailerUrl;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть не менее 1 минуты")
    private Integer duration; // Изменим с Short на Integer

    @NotNull(message = "ID режиссера обязателен")
    private Integer directorId;

    @NotNull(message = "ID страны обязателен")
    private Short countryId;

    @NotBlank(message = "Возрастной рейтинг обязателен")
    private String ageRating;

    private List<String> genres;
    private List<SessionDto> sessionList;

    private String directorName;
    private String countryName;
    private Integer sessionCount;
    private Boolean hasUpcomingSessions;
}