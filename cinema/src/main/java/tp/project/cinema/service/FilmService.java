package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.dto.Mapping.FilmMapping;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.AgeRating;
import tp.project.cinema.model.Country;
import tp.project.cinema.model.Director;
import tp.project.cinema.model.Film;
import tp.project.cinema.repository.AgeRatingRepository;
import tp.project.cinema.repository.CountryRepository;
import tp.project.cinema.repository.DirectorRepository;
import tp.project.cinema.repository.FilmRepository;
import tp.project.cinema.repository.SessionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FilmService {

    private final FilmRepository filmRepository;
    private final DirectorRepository directorRepository;
    private final CountryRepository countryRepository;
    private final AgeRatingRepository ageRatingRepository;
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

        if (title != null && !title.isEmpty() && genre != null && !genre.isEmpty() && ageRating != null && !ageRating.isEmpty()) {
            // Поиск по всем трем параметрам
            films = filmRepository.findByTitleContainingIgnoreCase(title);
            films = films.stream()
                    .filter(f -> f.getFilm_genre_list().stream()
                            .anyMatch(fg -> fg.getGenre().getGenre_name().equalsIgnoreCase(genre)))
                    .filter(f -> f.getAge_rating().getRating_value().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (title != null && !title.isEmpty() && genre != null && !genre.isEmpty()) {
            // Поиск по названию и жанру
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getFilm_genre_list().stream()
                            .anyMatch(fg -> fg.getGenre().getGenre_name().equalsIgnoreCase(genre)))
                    .collect(Collectors.toList());
        } else if (title != null && !title.isEmpty() && ageRating != null && !ageRating.isEmpty()) {
            // Поиск по названию и возрастному рейтингу
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getAge_rating().getRating_value().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (genre != null && !genre.isEmpty() && ageRating != null && !ageRating.isEmpty()) {
            // Поиск по жанру и возрастному рейтингу
            films = filmRepository.findByGenreName(genre).stream()
                    .filter(f -> f.getAge_rating().getRating_value().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (title != null && !title.isEmpty()) {
            // Поиск только по названию
            films = filmRepository.findByTitleContainingIgnoreCase(title);
        } else if (genre != null && !genre.isEmpty()) {
            // Поиск только по жанру
            films = filmRepository.findByGenreName(genre);
        } else if (ageRating != null && !ageRating.isEmpty()) {
            // Поиск только по возрастному рейтингу
            films = filmRepository.findByAgeRating(ageRating);
        } else {
            // Без параметров - возвращаем все фильмы
            films = filmRepository.findAll();
        }

        return films.stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getNowPlayingFilms() {
        List<Film> releasedFilms = filmRepository.findReleasedFilms();

        // Фильтруем только те фильмы, у которых есть активные сеансы
        return releasedFilms.stream()
                .filter(film -> {
                    List<tp.project.cinema.model.Session> sessions = sessionRepository.findByFilmFilmId(film.getFilm_id());
                    return sessions.stream()
                            .anyMatch(session -> session.getDate_time().isAfter(java.time.LocalDateTime.now())
                                    && !"CANCELLED".equals(session.getStatus()));
                })
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

    public FilmDto createFilm(FilmDto filmDto) {
        // Проверяем и находим режиссера
        Director director = directorRepository.findById(filmDto.getDirectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Режиссер с ID " + filmDto.getDirectorId() + " не найден"));

        // Проверяем и находим страну
        Country country = countryRepository.findById(filmDto.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Страна с ID " + filmDto.getCountryId() + " не найдена"));

        // Проверяем и находим возрастной рейтинг
        AgeRating ageRating = ageRatingRepository.findByRatingValue(filmDto.getAgeRating())
                .orElseGet(() -> {
                    // Создаем новый возрастной рейтинг, если не существует
                    AgeRating newRating = new AgeRating();
                    newRating.setRating_value(filmDto.getAgeRating());
                    return ageRatingRepository.save(newRating);
                });

        Film film = filmMapping.toEntity(filmDto);
        film.setDirector(director);
        film.setCountry(country);
        film.setAge_rating(ageRating);
        film.setFilm_genre_list(new ArrayList<>());
        film.setSession_list(new ArrayList<>());

        // Устанавливаем значения по умолчанию для обязательных полей
        // Для примитивного типа short используем значение по умолчанию, а не null-проверку
        if (film.getDuration() <= 0) {
            film.setDuration((short) 120); // длительность по умолчанию 2 часа
        }
        if (film.getRelease_date() == null) {
            film.setRelease_date(LocalDate.now());
        }

        Film savedFilm = filmRepository.save(film);
        return filmMapping.toDto(savedFilm);
    }

    public FilmDto updateFilm(Long id, FilmDto filmDto) {
        Film existingFilm = filmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с ID " + id + " не найден"));

        // Обновляем режиссера, если указан
        if (filmDto.getDirectorId() != null) {
            Director director = directorRepository.findById(filmDto.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Режиссер с ID " + filmDto.getDirectorId() + " не найден"));
            existingFilm.setDirector(director);
        }

        // Обновляем страну, если указана
        if (filmDto.getCountryId() != null) {
            Country country = countryRepository.findById(filmDto.getCountryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Страна с ID " + filmDto.getCountryId() + " не найдена"));
            existingFilm.setCountry(country);
        }

        // Обновляем возрастной рейтинг, если указан
        if (filmDto.getAgeRating() != null && !filmDto.getAgeRating().isEmpty()) {
            AgeRating ageRating = ageRatingRepository.findByRatingValue(filmDto.getAgeRating())
                    .orElseGet(() -> {
                        AgeRating newRating = new AgeRating();
                        newRating.setRating_value(filmDto.getAgeRating());
                        return ageRatingRepository.save(newRating);
                    });
            existingFilm.setAge_rating(ageRating);
        }

        // Обновляем остальные поля
        if (filmDto.getTitle() != null && !filmDto.getTitle().isEmpty()) {
            existingFilm.setTitle(filmDto.getTitle());
        }
        if (filmDto.getDescription() != null && !filmDto.getDescription().isEmpty()) {
            existingFilm.setDescription(filmDto.getDescription());
        }
        if (filmDto.getDuration() != null) {
            existingFilm.setDuration(filmDto.getDuration());
        }
        if (filmDto.getReleaseDate() != null) {
            existingFilm.setRelease_date(filmDto.getReleaseDate());
        }
        if (filmDto.getPosterUrl() != null) {
            existingFilm.setPoster_url(filmDto.getPosterUrl());
        }
        if (filmDto.getTrailerUrl() != null) {
            existingFilm.setTrailer_url(filmDto.getTrailerUrl());
        }

        Film updatedFilm = filmRepository.save(existingFilm);
        return filmMapping.toDto(updatedFilm);
    }

    public void deleteFilm(Long id) {
        if (!filmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Фильм с ID " + id + " не найден");
        }
        filmRepository.deleteById(id);
    }

    public List<FilmDto> getActiveFilms() {
        // Фильмы с активными (не отмененными) сеансами в будущем
        List<Film> allFilms = filmRepository.findAll();

        return allFilms.stream()
                .filter(film -> {
                    List<tp.project.cinema.model.Session> sessions = sessionRepository.findByFilmFilmId(film.getFilm_id());
                    return sessions.stream()
                            .anyMatch(session -> session.getDate_time().isAfter(java.time.LocalDateTime.now())
                                    && !"CANCELLED".equals(session.getStatus()));
                })
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> searchByKeyword(String keyword) {
        return filmRepository.searchByKeyword(keyword).stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }
}