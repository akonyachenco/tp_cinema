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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // Получить все сеансы
    @GetMapping
    public ResponseEntity<List<SessionDto>> getAllSessions() {
        List<SessionDto> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    // Получить сеанс по ID
    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> getSessionById(@PathVariable Integer id) {
        SessionDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    // Получить сеансы фильма
    @GetMapping("/movie/{filmId}")
    public ResponseEntity<List<SessionDto>> getSessionsByFilm(@PathVariable Long filmId) {
        List<SessionDto> sessions = sessionService.getSessionsByFilm(filmId);
        return ResponseEntity.ok(sessions);
    }

    // Получить сеансы зала
    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<SessionDto>> getSessionsByHall(@PathVariable Short hallId) {
        List<SessionDto> sessions = sessionService.getSessionsByHall(hallId);
        return ResponseEntity.ok(sessions);
    }

    // Получить сеансы по дате
    @GetMapping("/date")
    public ResponseEntity<List<SessionDto>> getSessionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime date) {
        List<SessionDto> sessions = sessionService.getSessionsByDate(date);
        return ResponseEntity.ok(sessions);
    }

    // Получить доступные места сеанса
    @GetMapping("/{sessionId}/available-seats")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@PathVariable Integer sessionId) {
        List<SeatDto> availableSeats = sessionService.getAvailableSeats(sessionId);
        return ResponseEntity.ok(availableSeats);
    }

    // Создать сеанс
    @PostMapping
    public ResponseEntity<SessionDto> createSession(
            @Valid @RequestBody SessionDto sessionDto) {
        SessionDto createdSession = sessionService.createSession(sessionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    // Обновить сеанс
    @PutMapping("/{id}")
    public ResponseEntity<SessionDto> updateSession(
            @PathVariable Integer id,
            @Valid @RequestBody SessionDto sessionDto) {
        SessionDto updatedSession = sessionService.updateSession(id, sessionDto);
        return ResponseEntity.ok(updatedSession);
    }

    // Удалить сеанс
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Integer id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    // Отменить сеанс
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<SessionDto> cancelSession(@PathVariable Integer id) {
        SessionDto cancelledSession = sessionService.cancelSession(id);
        return ResponseEntity.ok(cancelledSession);
    }

    // Получить доступные сеансы
    @GetMapping("/available")
    public ResponseEntity<List<SessionDto>> getAvailableSessions() {
        List<SessionDto> sessions = sessionService.getAvailableSessions();
        return ResponseEntity.ok(sessions);
    }

    // Получить сеансы фильма на определенную дату
    @GetMapping("/movie/{filmId}/date/{date}")
    public ResponseEntity<List<SessionDto>> getSessionsByMovieAndDate(
            @PathVariable Long filmId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SessionDto> sessions = sessionService.getSessionsByMovieAndDate(filmId, date);
        return ResponseEntity.ok(sessions);
    }

    // Получить детальную информацию о сеансе
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getSessionDetails(@PathVariable Integer id) {
        Map<String, Object> details = sessionService.getSessionDetails(id);
        return ResponseEntity.ok(details);
    }

    // Получить ближайшие сеансы
    @GetMapping("/upcoming")
    public ResponseEntity<List<SessionDto>> getUpcomingSessions(
            @RequestParam(defaultValue = "7") Integer days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);

        List<SessionDto> allSessions = sessionService.getAllSessions();
        List<SessionDto> upcomingSessions = allSessions.stream()
                .filter(s -> s.getDateTime() != null &&
                        s.getDateTime().isAfter(now) &&
                        s.getDateTime().isBefore(future))
                .toList();

        return ResponseEntity.ok(upcomingSessions);
    }

    // Получить сеансы на сегодня
    @GetMapping("/today")
    public ResponseEntity<List<SessionDto>> getTodaySessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atStartOfDay().plusDays(1);

        List<SessionDto> sessions = sessionService.getAllSessions().stream()
                .filter(s -> s.getDateTime() != null &&
                        s.getDateTime().isAfter(now) &&
                        s.getDateTime().isBefore(endOfDay))
                .toList();

        return ResponseEntity.ok(sessions);
    }

    // Получить сеансы на завтра
    @GetMapping("/tomorrow")
    public ResponseEntity<List<SessionDto>> getTomorrowSessions() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfTomorrow = tomorrow.plusDays(1);

        List<SessionDto> sessions = sessionService.getAllSessions().stream()
                .filter(s -> s.getDateTime() != null &&
                        s.getDateTime().isAfter(tomorrow) &&
                        s.getDateTime().isBefore(endOfTomorrow))
                .toList();

        return ResponseEntity.ok(sessions);
    }

    // Получить расписание на неделю
    @GetMapping("/week")
    public ResponseEntity<Map<String, List<SessionDto>>> getWeekSessions() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, List<SessionDto>> weekSchedule = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDateTime dayStart = now.plusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            List<SessionDto> daySessions = sessionService.getAllSessions().stream()
                    .filter(s -> s.getDateTime() != null &&
                            s.getDateTime().isAfter(dayStart) &&
                            s.getDateTime().isBefore(dayEnd))
                    .toList();

            weekSchedule.put(dayStart.toLocalDate().toString(), daySessions);
        }

        return ResponseEntity.ok(weekSchedule);
    }

    // Получить самый популярный сеанс (по количеству бронирований)
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getMostPopularSession(
            @RequestParam(defaultValue = "7") Integer days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<SessionDto> allSessions = sessionService.getAllSessions();

        // В реальном приложении нужно считать бронирования
        // Здесь упрощенная версия
        SessionDto popularSession = allSessions.stream()
                .filter(s -> s.getDateTime() != null && s.getDateTime().isAfter(startDate))
                .findFirst()
                .orElse(null);

        Map<String, Object> response = new HashMap<>();
        if (popularSession != null) {
            response.put("session", popularSession);
            response.put("period", days + " days");
        } else {
            response.put("message", "No sessions found");
        }

        return ResponseEntity.ok(response);
    }

    // Получить статистику по сеансам
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSessionsStatistics() {
        List<SessionDto> allSessions = sessionService.getAllSessions();
        LocalDateTime now = LocalDateTime.now();

        long totalSessions = allSessions.size();
        long upcomingSessions = allSessions.stream()
                .filter(s -> s.getDateTime() != null && s.getDateTime().isAfter(now))
                .count();
        long pastSessions = allSessions.stream()
                .filter(s -> s.getDateTime() != null && s.getDateTime().isBefore(now))
                .count();
        long cancelledSessions = allSessions.stream()
                .filter(s -> "CANCELLED".equals(s.getStatus()))
                .count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSessions", totalSessions);
        statistics.put("upcomingSessions", upcomingSessions);
        statistics.put("pastSessions", pastSessions);
        statistics.put("cancelledSessions", cancelledSessions);
        statistics.put("activeSessions", upcomingSessions - cancelledSessions);

        return ResponseEntity.ok(statistics);
    }

    // Поиск сеансов по фильтрам
    @GetMapping("/search")
    public ResponseEntity<List<SessionDto>> searchSessions(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false) Short hallId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<SessionDto> allSessions = sessionService.getAllSessions();

        List<SessionDto> filteredSessions = allSessions.stream()
                .filter(session -> {
                    boolean matches = true;

                    if (filmId != null) {
                        matches = matches && filmId.equals(session.getFilmId());
                    }

                    if (hallId != null) {
                        matches = matches && hallId.equals(session.getHallId());
                    }

                    if (date != null && session.getDateTime() != null) {
                        matches = matches && date.equals(session.getDateTime().toLocalDate());
                    }

                    if (startTime != null && session.getDateTime() != null) {
                        matches = matches && !session.getDateTime().isBefore(startTime);
                    }

                    if (endTime != null && session.getDateTime() != null) {
                        matches = matches && !session.getDateTime().isAfter(endTime);
                    }

                    return matches;
                })
                .toList();

        return ResponseEntity.ok(filteredSessions);
    }

    // Получить сеансы с лучшей доступностью (больше всего свободных мест)
    @GetMapping("/best-availability")
    public ResponseEntity<List<SessionDto>> getSessionsWithBestAvailability() {
        List<SessionDto> availableSessions = sessionService.getAvailableSessions();

        // Сортируем по времени (ближайшие первые)
        List<SessionDto> sortedSessions = availableSessions.stream()
                .sorted((s1, s2) -> {
                    if (s1.getDateTime() == null) return 1;
                    if (s2.getDateTime() == null) return -1;
                    return s1.getDateTime().compareTo(s2.getDateTime());
                })
                .toList();

        return ResponseEntity.ok(sortedSessions);
    }
}