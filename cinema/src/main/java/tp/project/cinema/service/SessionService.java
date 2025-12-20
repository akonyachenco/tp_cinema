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
import tp.project.cinema.repository.TicketRepository;

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
    private final TicketRepository ticketRepository;
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

        Short hallId = session.getHall().getHallId();

        return seatRepository.findAvailableSeatsForSession(hallId, sessionId).stream()
                .map(seat -> {
                    SeatDto dto = new SeatDto();
                    dto.setSeatId(seat.getSeatId());
                    dto.setRowNumber(seat.getRowNumber());
                    dto.setSeatNumber(seat.getSeatNumber());
                    dto.setSeatType(seat.getSeatType().getTypeName());
                    dto.setPriceMultiplier(seat.getSeatType().getPriceMultiplier());
                    dto.setHallId(seat.getHall().getHallId());
                    dto.setBasePrice(seat.getHall().getBasePrice());
                    dto.setHallName(seat.getHall().getHallName());
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

        Session previousSession = sessionRepository.findPreviousSession(
                sessionDto.getHallId(),
                sessionDto.getDateTime()
        ).orElse(null);

        if (previousSession != null && previousSession.getDateTime().plusMinutes(
                previousSession.getFilm().getDuration() + 20).isAfter(sessionDto.getDateTime()))
            throw new IllegalArgumentException("Данный сеанс перекрывает предыдущий сеанс");


        // Проверяем конфликт времени
        List<Session> conflictingSessions = sessionRepository.findConflictingSessions(
                sessionDto.getHallId(),
                sessionDto.getDateTime(),
                sessionDto.getDateTime().plusMinutes(film.getDuration() + 20)
        );

        if (!conflictingSessions.isEmpty()) {
            throw new IllegalArgumentException("Данный сеанс перекрывает следующий сеанс");
        }

        Session session = sessionMapping.toEntity(sessionDto);
        session.setFilm(film);
        session.setHall(hall);
        session.setStatus("Запланирован");

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

        existingSession.setDateTime(sessionDto.getDateTime());

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

        session.setStatus("Отменен");
        Session cancelledSession = sessionRepository.save(session);
        return sessionMapping.toDto(cancelledSession);
    }

    // ДОБАВЛЕННЫЕ МЕТОДЫ ДЛЯ ФРОНТЕНДА:

    public List<SessionDto> getAvailableSessions() {
        return sessionRepository.findByDateTimeAfter(LocalDateTime.now()).stream()
                .filter(session -> !"Отменен".equals(session.getStatus()))
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionDto> getSessionsByMovieAndDate(Long filmId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return sessionRepository.findByFilmFilmId(filmId).stream()
                .filter(session ->
                        session.getDateTime().isAfter(startOfDay) &&
                                session.getDateTime().isBefore(endOfDay) &&
                                !"Отменен".equals(session.getStatus())
                )
                .map(sessionMapping::toDto)
                .collect(Collectors.toList());
    }

    // Метод для получения деталей сеанса
    public Map<String, Object> getSessionDetails(Integer id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + id + " не найден"));

        Map<String, Object> details = new HashMap<>();
        details.put("session", sessionMapping.toDto(session));
        details.put("film", session.getFilm());
        details.put("hall", session.getHall());
        details.put("availableSeats", getAvailableSeats(id).size());
        details.put("totalSeats", seatRepository.countSeatsByHall(session.getHall().getHallId()));

        return details;
    }

    // Вспомогательные методы, которые должны быть в SessionService

    // Получить места зала
    private List<SeatDto> getSeatsByHall(Short hallId) {
        return seatRepository.findByHallHallId(hallId).stream()
                .map(seat -> {
                    SeatDto dto = new SeatDto();
                    dto.setSeatId(seat.getSeatId());
                    dto.setRowNumber(seat.getRowNumber());
                    dto.setSeatNumber(seat.getSeatNumber());
                    dto.setSeatType(seat.getSeatType().getTypeName());
                    dto.setPriceMultiplier(seat.getSeatType().getPriceMultiplier());
                    dto.setHallId(seat.getHall().getHallId());
                    dto.setBasePrice(seat.getHall().getBasePrice());
                    dto.setHallName(seat.getHall().getHallName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Получить занятые места для сеанса
    private List<SeatDto> getBookedSeatsForSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден"));

        Short hallId = session.getHall().getHallId();

        return seatRepository.findBookedSeatsForSession(hallId, sessionId).stream()
                .map(seat -> {
                    SeatDto dto = new SeatDto();
                    dto.setSeatId(seat.getSeatId());
                    dto.setRowNumber(seat.getRowNumber());
                    dto.setSeatNumber(seat.getSeatNumber());
                    dto.setSeatType(seat.getSeatType().getTypeName());
                    dto.setPriceMultiplier(seat.getSeatType().getPriceMultiplier());
                    dto.setHallId(seat.getHall().getHallId());
                    dto.setBasePrice(seat.getHall().getBasePrice());
                    dto.setHallName(seat.getHall().getHallName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Получить доступные места для сеанса (вспомогательный)
    private List<SeatDto> getAvailableSeatsForSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден"));

        Short hallId = session.getHall().getHallId();

        return seatRepository.findAvailableSeatsForSession(hallId, sessionId).stream()
                .map(seat -> {
                    SeatDto dto = new SeatDto();
                    dto.setSeatId(seat.getSeatId());
                    dto.setRowNumber(seat.getRowNumber());
                    dto.setSeatNumber(seat.getSeatNumber());
                    dto.setSeatType(seat.getSeatType().getTypeName());
                    dto.setPriceMultiplier(seat.getSeatType().getPriceMultiplier());
                    dto.setHallId(seat.getHall().getHallId());
                    dto.setBasePrice(seat.getHall().getBasePrice());
                    dto.setHallName(seat.getHall().getHallName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}