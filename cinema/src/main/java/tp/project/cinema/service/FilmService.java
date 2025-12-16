package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.dto.Mapping.FilmMapping;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Film;
import tp.project.cinema.repository.FilmRepository;
import tp.project.cinema.repository.SessionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FilmService {

    private final FilmRepository filmRepository;
    private final SessionRepository sessionRepository;
    private final FilmMapping filmMapping;

    public List<FilmDto> getAllFilms() {
        return filmRepository.findAll().stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с ID " + id + " не найден"));
        return filmMapping.toDto(film);
    }

    public List<FilmDto> searchFilms(String title, String genre, String ageRating) {
        List<Film> films;

        if (title != null && genre != null && ageRating != null) {
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getFilm_genre_list().stream()
                            .anyMatch(fg -> fg.getGenre().getGenre_name().equalsIgnoreCase(genre)))
                    .filter(f -> f.getAge_rating().getRating_value().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (title != null && genre != null) {
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getFilm_genre_list().stream()
                            .anyMatch(fg -> fg.getGenre().getGenre_name().equalsIgnoreCase(genre)))
                    .collect(Collectors.toList());
        } else if (title != null && ageRating != null) {
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getAge_rating().getRating_value().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (genre != null && ageRating != null) {
            films = filmRepository.findByGenreName(genre).stream()
                    .filter(f -> f.getAge_rating().getRating_value().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (title != null) {
            films = filmRepository.findByTitleContainingIgnoreCase(title);
        } else if (genre != null) {
            films = filmRepository.findByGenreName(genre);
        } else if (ageRating != null) {
            films = filmRepository.findByAgeRating(ageRating);
        } else {
            films = filmRepository.findAll();
        }

        return films.stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getNowPlayingFilms() {
        return filmRepository.findReleasedFilms().stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getComingSoonFilms() {
        return filmRepository.findUpcomingFilms().stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByReleaseDate(LocalDate date) {
        List<Film> films = filmRepository.findByReleaseDateBetween(date, date);
        return films.stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionDto> getFilmSessions(Long filmId) {
        if (!filmRepository.existsById(filmId)) {
            throw new ResourceNotFoundException("Фильм с ID " + filmId + " не найден");
        }

        return sessionRepository.findByFilmFilmId(filmId).stream()
                .map(session -> {
                    SessionDto dto = new SessionDto();
                    dto.setSessionId(session.getSession_id());
                    dto.setDateTime(session.getDate_time());
                    dto.setStatus(session.getStatus());
                    dto.setFilmId(session.getFilm().getFilm_id());
                    dto.setHallId(session.getHall().getHall_id());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}