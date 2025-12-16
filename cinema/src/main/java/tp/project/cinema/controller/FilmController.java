package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.FilmDto;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.service.FilmService;

import java.time.LocalDate;
import java.util.List;

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
}