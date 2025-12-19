package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.DirectorDto;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.dto.FilmInfoListDto;
import tp.project.cinema.dto.Mapping.CountryMapping;
import tp.project.cinema.dto.Mapping.DirectorMapping;
import tp.project.cinema.dto.Mapping.FilmMapping;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.exception.AlreadyExistsException;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.*;
import tp.project.cinema.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final GenreRepository genreRepository;
    private final FilmGenreRepository filmGenreRepository;
    private final FilmMapping filmMapping;
    private final CountryMapping countryMapping;
    private final DirectorMapping directorMapping;

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
                    .filter(f -> f.getFilmGenreList().stream()
                            .anyMatch(fg -> fg.getGenre().getGenreName().equalsIgnoreCase(genre)))
                    .filter(f -> f.getAgeRating().getRatingValue().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (title != null && !title.isEmpty() && genre != null && !genre.isEmpty()) {
            // Поиск по названию и жанру
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getFilmGenreList().stream()
                            .anyMatch(fg -> fg.getGenre().getGenreName().equalsIgnoreCase(genre)))
                    .collect(Collectors.toList());
        } else if (title != null && !title.isEmpty() && ageRating != null && !ageRating.isEmpty()) {
            // Поиск по названию и возрастному рейтингу
            films = filmRepository.findByTitleContainingIgnoreCase(title).stream()
                    .filter(f -> f.getAgeRating().getRatingValue().equalsIgnoreCase(ageRating))
                    .collect(Collectors.toList());
        } else if (genre != null && !genre.isEmpty() && ageRating != null && !ageRating.isEmpty()) {
            // Поиск по жанру и возрастному рейтингу
            films = filmRepository.findByGenreName(genre).stream()
                    .filter(f -> f.getAgeRating().getRatingValue().equalsIgnoreCase(ageRating))
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
                    List<Session> sessions = sessionRepository.findByFilmFilmId(film.getFilmId());
                    return sessions.stream()
                            .anyMatch(session -> session.getDateTime().isAfter(java.time.LocalDateTime.now())
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
                    dto.setSessionId(session.getSessionId());
                    dto.setDateTime(session.getDateTime());
                    dto.setStatus(session.getStatus());
                    dto.setFilmId(session.getFilm().getFilmId());
                    dto.setHallId(session.getHall().getHallId());
                    dto.setFilmTitle(session.getFilm().getTitle());
                    dto.setHallName(session.getHall().getHallName());
                    dto.setDuration((int) session.getFilm().getDuration());
                    dto.setBasePrice(session.getHall().getBasePrice().doubleValue());
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
                    newRating.setRatingValue(filmDto.getAgeRating());
                    return ageRatingRepository.save(newRating);
                });

        Film film = filmMapping.toEntity(filmDto);
        film.setDirector(director);
        film.setCountry(country);
        film.setAgeRating(ageRating);
        film.setFilmGenreList(new ArrayList<>());
        film.setSessionList(new ArrayList<>());

        // Устанавливаем значения по умолчанию для обязательных полей
        if (film.getDuration() <= 0) {
            film.setDuration((short) 120); // длительность по умолчанию 2 часа
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(LocalDate.now());
        }

        Film savedFilm = filmRepository.save(film);

        // Добавляем жанры
        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            addGenresToFilm(savedFilm, filmDto.getGenres());
        }

        return filmMapping.toDto(savedFilm);
    }

    private void addGenresToFilm(Film film, List<String> genreNames) {
        for (String genreName : genreNames) {
            Genre genre = genreRepository.findByGenreName(genreName)
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setGenreName(genreName);
                        return genreRepository.save(newGenre);
                    });

            // Проверяем, не добавлен ли уже этот жанр
            if (!filmGenreRepository.existsByFilmFilmIdAndGenreGenreId(film.getFilmId(), genre.getGenreId())) {
                FilmGenre filmGenre = new FilmGenre();
                filmGenre.setFilm(film);
                filmGenre.setGenre(genre);
                filmGenreRepository.save(filmGenre);
            }
        }
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
                        newRating.setRatingValue(filmDto.getAgeRating());
                        return ageRatingRepository.save(newRating);
                    });
            existingFilm.setAgeRating(ageRating);
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
            existingFilm.setReleaseDate(filmDto.getReleaseDate());
        }
        if (filmDto.getPosterUrl() != null) {
            existingFilm.setPosterUrl(filmDto.getPosterUrl());
        }
        if (filmDto.getTrailerUrl() != null) {
            existingFilm.setTrailerUrl(filmDto.getTrailerUrl());
        }

        // Обновляем жанры
        if (filmDto.getGenres() != null) {
            // Удаляем старые связи
            filmGenreRepository.deleteByFilmId(id);

            // Добавляем новые жанры
            addGenresToFilm(existingFilm, filmDto.getGenres());
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
                    List<Session> sessions = sessionRepository.findByFilmFilmId(film.getFilmId());
                    return sessions.stream()
                            .anyMatch(session -> session.getDateTime().isAfter(java.time.LocalDateTime.now())
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

    // Дополнительные методы

    public List<FilmDto> getFilmsByDirector(Integer directorId) {
        return filmRepository.findByDirectorId(directorId).stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByCountry(Short countryId) {
        return filmRepository.findByCountryId(countryId).stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDurationRange(Short minDuration, Short maxDuration) {
        List<Film> films = filmRepository.findAll().stream()
                .filter(film -> film.getDuration() >= minDuration && film.getDuration() <= maxDuration)
                .collect(Collectors.toList());

        return films.stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsReleasedAfter(LocalDate date) {
        return filmRepository.findByReleaseDateAfter(date).stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsReleasedBefore(LocalDate date) {
        return filmRepository.findByReleaseDateBefore(date).stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsReleasedBetween(LocalDate startDate, LocalDate endDate) {
        return filmRepository.findByReleaseDateBetween(startDate, endDate).stream()
                .map(filmMapping::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getFilmStatistics(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с ID " + filmId + " не найден"));

        long sessionsCount = sessionRepository.countByFilmId(filmId);
        List<Session> upcomingSessions = sessionRepository.findUpcomingSessionsByFilm(filmId);
        List<Genre> genres = genreRepository.findByFilmId(filmId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("filmId", filmId);
        statistics.put("title", film.getTitle());
        statistics.put("duration", film.getDuration());
        statistics.put("releaseDate", film.getReleaseDate());
        statistics.put("ageRating", film.getAgeRating().getRatingValue());
        statistics.put("director", film.getDirector().getName() + " " + film.getDirector().getSurname());
        statistics.put("country", film.getCountry().getCountryName());
        statistics.put("totalSessions", sessionsCount);
        statistics.put("upcomingSessions", upcomingSessions.size());
        statistics.put("genres", genres.stream().map(Genre::getGenreName).collect(Collectors.toList()));
        statistics.put("hasUpcomingSessions", upcomingSessions.size() > 0);

        return statistics;
    }

    public List<FilmDto> getFilmsWithUpcomingSessions() {
        return sessionRepository.findFilmsWithUpcomingSessions().stream()
                .map(film -> {
                    FilmDto dto = new FilmDto();
                    dto.setFilmId(film.getFilmId());
                    dto.setTitle(film.getTitle());
                    dto.setDescription(film.getDescription());
                    dto.setDuration((int) film.getDuration());
                    dto.setReleaseDate(film.getReleaseDate());
                    dto.setPosterUrl(film.getPosterUrl());
                    dto.setTrailerUrl(film.getTrailerUrl());
                    dto.setDirectorId(film.getDirector().getDirectorId());
                    dto.setCountryId(film.getCountry().getCountryId());
                    dto.setAgeRating(film.getAgeRating().getRatingValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public FilmInfoListDto getCountriesAndDirectors() {
        FilmInfoListDto filmInfoListDto = new FilmInfoListDto();
        filmInfoListDto.setCountries(countryRepository.findAll()
                .stream()
                .map(countryMapping::toDto)
                .collect(Collectors.toList()));
        filmInfoListDto.setDirectors(directorRepository.findAll()
                .stream()
                .map(directorMapping::toDto)
                .collect(Collectors.toList()));
        return filmInfoListDto;
    }

    public DirectorDto createDirector(DirectorDto directorDto) {

        Director findDirector = directorRepository.findByNameAndSurnameContaining(directorDto.getDirectorNameAndSurname().split(" ")[1],
                directorDto.getDirectorNameAndSurname().split(" ")[0]);

        if(findDirector != null) {
            throw new AlreadyExistsException("Режиссёр с именем и фамилией " + directorDto.getDirectorNameAndSurname() + " уже существует");
            //return directorMapping.toDto(findDirector);
        }

        Country country = countryRepository.findById(directorDto.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Страна с ID " + directorDto.getCountryId() + " не найдена"));

        Director director = directorMapping.toEntity(directorDto);
        director.setCountry(country);
        director.setFilmList(new ArrayList<>());


        directorRepository.save(director);


        return directorMapping.toDto(director);
    }
}