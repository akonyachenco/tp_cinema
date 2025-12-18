package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.dto.FilmInfoListDto;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.service.FilmService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmDto>> getAllFilms() {
        List<FilmDto> films = filmService.getAllFilms();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilmById(@PathVariable Long id) {
        FilmDto film = filmService.getFilmById(id);
        return ResponseEntity.ok(film);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDto>> searchFilms(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String ageRating) {

        List<FilmDto> films = filmService.searchFilms(title, genre, ageRating);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/now-playing")
    public ResponseEntity<List<FilmDto>> getNowPlayingFilms() {
        List<FilmDto> films = filmService.getNowPlayingFilms();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<List<FilmDto>> getComingSoonFilms() {
        List<FilmDto> films = filmService.getComingSoonFilms();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<FilmDto>> getFilmsByReleaseDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        List<FilmDto> films = filmService.getFilmsByReleaseDate(date);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{filmId}/sessions")
    public ResponseEntity<List<SessionDto>> getFilmSessions(@PathVariable Long filmId) {
        List<SessionDto> sessions = filmService.getFilmSessions(filmId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping
    public ResponseEntity<FilmInfoListDto> getCountriesAndDirectors() {
        FilmInfoListDto filmsInfoList = filmService.getCountriesAndDirectors();
        return ResponseEntity.status(HttpStatus.OK).body(filmsInfoList);
    }

    @PostMapping
    public ResponseEntity<FilmDto> createFilm(@Valid @RequestBody FilmDto filmDto) {
        FilmDto createdFilm = filmService.createFilm(filmDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FilmDto> updateFilm(
            @PathVariable Long id,
            @Valid @RequestBody FilmDto filmDto) {
        FilmDto updatedFilm = filmService.updateFilm(id, filmDto);
        return ResponseEntity.ok(updatedFilm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long id) {
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<FilmDto>> getActiveFilms() {
        List<FilmDto> films = filmService.getActiveFilms();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<FilmDto>> searchFilmsByKeyword(
            @RequestParam String keyword) {
        List<FilmDto> films = filmService.searchByKeyword(keyword);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<FilmDto>> getFilmsByDirector(@PathVariable Integer directorId) {
        List<FilmDto> films = filmService.getFilmsByDirector(directorId);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<List<FilmDto>> getFilmsByCountry(@PathVariable Short countryId) {
        List<FilmDto> films = filmService.getFilmsByCountry(countryId);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/duration-range")
    public ResponseEntity<List<FilmDto>> getFilmsByDurationRange(
            @RequestParam Short minDuration,
            @RequestParam Short maxDuration) {
        List<FilmDto> films = filmService.getFilmsByDurationRange(minDuration, maxDuration);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/released-after")
    public ResponseEntity<List<FilmDto>> getFilmsReleasedAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<FilmDto> films = filmService.getFilmsReleasedAfter(date);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/released-before")
    public ResponseEntity<List<FilmDto>> getFilmsReleasedBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<FilmDto> films = filmService.getFilmsReleasedBefore(date);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/released-between")
    public ResponseEntity<List<FilmDto>> getFilmsReleasedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FilmDto> films = filmService.getFilmsReleasedBetween(startDate, endDate);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getFilmStatistics(@PathVariable Long id) {
        Map<String, Object> statistics = filmService.getFilmStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/with-upcoming-sessions")
    public ResponseEntity<List<FilmDto>> getFilmsWithUpcomingSessions() {
        List<FilmDto> films = filmService.getFilmsWithUpcomingSessions();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<FilmDto>> getFilmsByGenreName(@PathVariable String genreName) {
        List<FilmDto> films = filmService.searchFilms(null, genreName, null);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/age-rating/{ageRating}")
    public ResponseEntity<List<FilmDto>> getFilmsByAgeRating(@PathVariable String ageRating) {
        List<FilmDto> films = filmService.searchFilms(null, null, ageRating);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<List<FilmDto>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String ageRating,
            @RequestParam(required = false) Short minDuration,
            @RequestParam(required = false) Short maxDuration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<FilmDto> allFilms = filmService.getAllFilms();

        List<FilmDto> filteredFilms = allFilms.stream()
                .filter(film -> {
                    boolean matches = true;

                    if (title != null && !title.isEmpty()) {
                        matches = matches && film.getTitle().toLowerCase().contains(title.toLowerCase());
                    }

                    if (genre != null && !genre.isEmpty()) {
                        matches = matches && film.getGenres() != null &&
                                film.getGenres().stream().anyMatch(g -> g.equalsIgnoreCase(genre));
                    }

                    if (ageRating != null && !ageRating.isEmpty()) {
                        matches = matches && film.getAgeRating() != null &&
                                film.getAgeRating().equalsIgnoreCase(ageRating);
                    }

                    if (minDuration != null) {
                        matches = matches && film.getDuration() != null &&
                                film.getDuration() >= minDuration;
                    }

                    if (maxDuration != null) {
                        matches = matches && film.getDuration() != null &&
                                film.getDuration() <= maxDuration;
                    }

                    if (startDate != null) {
                        matches = matches && film.getReleaseDate() != null &&
                                !film.getReleaseDate().isBefore(startDate);
                    }

                    if (endDate != null) {
                        matches = matches && film.getReleaseDate() != null &&
                                !film.getReleaseDate().isAfter(endDate);
                    }

                    return matches;
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(filteredFilms);
    }
}