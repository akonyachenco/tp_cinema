package tp.project.cinema.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.service.SessionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionDto>> getAllSessions() {
        List<SessionDto> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> getSessionById(@PathVariable Integer id) {
        SessionDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/by-film/{filmId}")
    public ResponseEntity<List<SessionDto>> getSessionsByFilm(@PathVariable Long filmId) {
        List<SessionDto> sessions = sessionService.getSessionsByFilm(filmId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/by-hall/{hallId}")
    public ResponseEntity<List<SessionDto>> getSessionsByHall(@PathVariable Short hallId) {
        List<SessionDto> sessions = sessionService.getSessionsByHall(hallId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<SessionDto>> getSessionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime date) {

        List<SessionDto> sessions = sessionService.getSessionsByDate(date);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/available-seats/{sessionId}")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@PathVariable Integer sessionId) {
        List<SeatDto> availableSeats = sessionService.getAvailableSeats(sessionId);
        return ResponseEntity.ok(availableSeats);
    }

    @PostMapping
    public ResponseEntity<SessionDto> createSession(
            @Valid @RequestBody SessionDto sessionDto) {
        SessionDto createdSession = sessionService.createSession(sessionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionDto> updateSession(
            @PathVariable Integer id,
            @Valid @RequestBody SessionDto sessionDto) {
        SessionDto updatedSession = sessionService.updateSession(id, sessionDto);
        return ResponseEntity.ok(updatedSession);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Integer id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<SessionDto> cancelSession(@PathVariable Integer id) {
        SessionDto cancelledSession = sessionService.cancelSession(id);
        return ResponseEntity.ok(cancelledSession);
    }
}