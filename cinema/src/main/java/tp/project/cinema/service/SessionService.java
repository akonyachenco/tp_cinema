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

import java.time.LocalDateTime;
import java.util.List;
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
        existingSession.setStatus(sessionDto.getStatus());
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
}