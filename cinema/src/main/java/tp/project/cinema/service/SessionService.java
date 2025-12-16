package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.SessionDto;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.dto.Mapping.SessionMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Film;
import tp.project.cinema.model.Hall;
import tp.project.cinema.model.Session;
import tp.project.cinema.repository.FilmRepository;
import tp.project.cinema.repository.HallRepository;
import tp.project.cinema.repository.SessionRepository;
import tp.project.cinema.repository.SeatRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final FilmRepository filmRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final SessionMapping sessionMapping;

    public List<SessionDto> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    public SessionDto getSessionById(Integer id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + id + " не найден"));
        return sessionMapping.toDto(session);
    }

    public List<SessionDto> getSessionsByFilm(Long filmId) {
        if (!filmRepository.existsById(filmId)) {
            throw new ResourceNotFoundException("Фильм с ID " + filmId + " не найден");
        }

        return sessionRepository.findByFilmFilmId(filmId).stream()
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionDto> getSessionsByHall(Short hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        return sessionRepository.findByHallHallId(hallId).stream()
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionDto> getSessionsByDate(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return sessionRepository.findByDateTimeBetween(startOfDay, endOfDay).stream()
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SeatDto> getAvailableSeats(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден"));

        Short hallId = session.getHall().getHall_id();

        return seatRepository.findAvailableSeatsForSession(hallId, sessionId).stream()
                .map(seat -> {
                    SeatDto dto = new SeatDto();
                    dto.setSeatId(seat.getSeat_id());
                    dto.setRowNumber(seat.getRow_number());
                    dto.setSeatNumber(seat.getSeat_number());
                    dto.setSeatType(seat.getSeat_type().getType_name());
                    dto.setPriceMultiplier(seat.getSeat_type().getPrice_multiplier());
                    dto.setHallId(seat.getHall().getHall_id());
                    dto.setBasePrice(seat.getHall().getBase_price());
                    dto.setHallName(seat.getHall().getHall_name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public SessionDto createSession(SessionDto sessionDto) {
        // Проверяем существование фильма
        Film film = filmRepository.findById(sessionDto.getFilmId())
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с ID " + sessionDto.getFilmId() + " не найден"));

        // Проверяем существование зала
        Hall hall = hallRepository.findById(sessionDto.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + sessionDto.getHallId() + " не найден"));

        // Проверяем, что hallId не null
        if (sessionDto.getHallId() == null || sessionDto.getHallId() == 0) {
            throw new IllegalArgumentException("ID зала обязателен");
        }

        // Проверяем, что filmId не null
        if (sessionDto.getFilmId() == null || sessionDto.getFilmId() == 0) {
            throw new IllegalArgumentException("ID фильма обязателен");
        }

        // Проверяем конфликт времени
        List<Session> conflictingSessions = sessionRepository.findConflictingSessions(
                sessionDto.getHallId(),
                sessionDto.getDateTime(),
                sessionDto.getDateTime().plusMinutes(film.getDuration() + 30) // +30 минут на уборку
        );

        if (!conflictingSessions.isEmpty()) {
            throw new IllegalArgumentException("В это время в зале уже запланирован другой сеанс");
        }

        Session session = sessionMapping.toEntity(sessionDto);
        session.setFilm(film);
        session.setHall(hall);
        session.setStatus("SCHEDULED");

        Session savedSession = sessionRepository.save(session);
        return sessionMapping.toDto(savedSession);
    }

    public SessionDto updateSession(Integer id, SessionDto sessionDto) {
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + id + " не найден"));

        Film film = filmRepository.findById(sessionDto.getFilmId())
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с ID " + sessionDto.getFilmId() + " не найден"));

        Hall hall = hallRepository.findById(sessionDto.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + sessionDto.getHallId() + " не найден"));

        existingSession.setDate_time(sessionDto.getDateTime());

        // Обновляем статус, если указан
        if (sessionDto.getStatus() != null && !sessionDto.getStatus().isEmpty()) {
            existingSession.setStatus(sessionDto.getStatus());
        }

        existingSession.setFilm(film);
        existingSession.setHall(hall);

        Session updatedSession = sessionRepository.save(existingSession);
        return sessionMapping.toDto(updatedSession);
    }

    public void deleteSession(Integer id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Сеанс с ID " + id + " не найден");
        }
        sessionRepository.deleteById(id);
    }

    public SessionDto cancelSession(Integer id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + id + " не найден"));

        session.setStatus("CANCELLED");
        Session cancelledSession = sessionRepository.save(session);
        return sessionMapping.toDto(cancelledSession);
    }

    // 🔥 НОВЫЕ МЕТОДЫ ДЛЯ ФРОНТЕНДА:

    public List<SessionDto> getAvailableSessions() {
        return sessionRepository.findByDateTimeAfter(LocalDateTime.now()).stream()
                .filter(session -> !"CANCELLED".equals(session.getStatus()))
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionDto> getSessionsByMovieAndDate(Long filmId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return sessionRepository.findByFilmFilmId(filmId).stream()
                .filter(session ->
                        session.getDate_time().isAfter(startOfDay) &&
                                session.getDate_time().isBefore(endOfDay) &&
                                !"CANCELLED".equals(session.getStatus())
                )
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    // Метод getHallLayout()
    public Map<String, Object> getHallLayout(Short hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + hallId + " не найден"));

        List<SeatDto> seats = getSeatsByHall(hallId);

        // Создаем Map с правильными типами
        Map<String, Object> layout = new HashMap<>();
        layout.put("hallId", (int) hall.getHall_id()); // short -> int
        layout.put("hallName", hall.getHall_name());
        layout.put("rowsCount", (int) hall.getRows_count()); // short -> int
        layout.put("seatsPerRow", (int) hall.getSeats_per_row()); // short -> int
        layout.put("seats", seats);
        layout.put("totalSeats", seats.size());

        return layout;
    }

    // Метод getSeatsForBooking()
    public Map<String, Object> getSeatsForBooking(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден");
        }

        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден"));

        Short hallId = session.getHall().getHall_id();

        List<SeatDto> allSeats = getSeatsByHall(hallId);
        List<SeatDto> bookedSeats = getBookedSeatsForSession(sessionId);
        List<SeatDto> availableSeats = getAvailableSeatsForSession(sessionId);

        // Помечаем статус каждого места
        allSeats.forEach(seat -> {
            boolean isBooked = bookedSeats.stream()
                    .anyMatch(booked -> booked.getSeatId().equals(seat.getSeatId()));
            seat.setStatus(isBooked ? "BOOKED" : "AVAILABLE");
        });

        // Создаем Map с правильными типами
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("filmId", session.getFilm().getFilm_id());
        result.put("filmTitle", session.getFilm().getTitle());
        result.put("sessionDateTime", session.getDate_time());
        result.put("hallId", (int) hallId); // short -> int
        result.put("hallName", session.getHall().getHall_name());
        result.put("allSeats", allSeats);
        result.put("bookedSeats", bookedSeats);
        result.put("availableSeats", availableSeats);
        result.put("totalSeats", allSeats.size());
        result.put("bookedCount", bookedSeats.size());
        result.put("availableCount", availableSeats.size());

        return result;
    }
}