package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.dto.Mapping.SeatMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Seat;
import tp.project.cinema.model.Hall;
import tp.project.cinema.repository.SeatRepository;
import tp.project.cinema.repository.SessionRepository;
import tp.project.cinema.repository.TicketRepository;
import tp.project.cinema.repository.HallRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {

    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final HallRepository hallRepository;
    private final SeatMapping seatMapping;

    // Получить все места зала
    public List<SeatDto> getSeatsByHall(Short hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        return seatRepository.findByHallHallId(hallId).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    // Получить место по ID
    public SeatDto getSeatById(Integer id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Место с ID " + id + " не найден"));
        return seatMapping.toDto(seat);
    }

    // Получить доступные места для сеанса
    public List<SeatDto> getAvailableSeatsForSession(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден");
        }

        // Получаем hallId из сеанса
        Short hallId = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден"))
                .getHall().getHall_id();

        return seatRepository.findAvailableSeatsForSession(hallId, sessionId).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    // Получить занятые места для сеанса
    public List<SeatDto> getBookedSeatsForSession(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден");
        }

        Short hallId = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден"))
                .getHall().getHall_id();

        return seatRepository.findBookedSeatsForSession(hallId, sessionId).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    // Получить места по ряду и залу
    public List<SeatDto> getSeatsByRow(Short rowNumber, Short hallId) {
        if (!seatRepository.existsByHallHallId(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        return seatRepository.findByRowNumberAndHallHallId(rowNumber, hallId).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    // Получить места по типу
    public List<SeatDto> getSeatsByType(String seatType) {
        return seatRepository.findBySeatTypeTypeName(seatType).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    // Получить места по диапазону цен
    public List<SeatDto> getSeatsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return seatRepository.findAll().stream()
                .filter(seat -> {
                    BigDecimal price = seat.getHall().getBase_price()
                            .multiply(seat.getSeat_type().getPrice_multiplier());
                    return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
                })
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    // Получить схему мест зала
    public Map<String, Object> getHallLayout(Short hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + hallId + " не найден"));

        List<SeatDto> seats = getSeatsByHall(hallId);

        // Создаем структуру для фронтенда
        Map<String, Object> layout = new HashMap<>();
        layout.put("hallId", hall.getHall_id());
        layout.put("hallName", hall.getHall_name());
        layout.put("rowsCount", (int) hall.getRows_count());
        layout.put("seatsPerRow", (int) hall.getSeats_per_row());
        layout.put("seats", seats);
        layout.put("totalSeats", seats.size());

        return layout;
    }

    // Получить места для бронирования сеанса
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

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("filmId", session.getFilm().getFilm_id());
        result.put("filmTitle", session.getFilm().getTitle());
        result.put("sessionDateTime", session.getDate_time());
        result.put("hallId", hallId);
        result.put("hallName", session.getHall().getHall_name());
        result.put("allSeats", allSeats);
        result.put("bookedSeats", bookedSeats);
        result.put("availableSeats", availableSeats);
        result.put("totalSeats", allSeats.size());
        result.put("bookedCount", bookedSeats.size());
        result.put("availableCount", availableSeats.size());

        return result;
    }

    // Получить лучшие места в зале (первые ряды)
    public List<SeatDto> getBestSeats(Short hallId) {
        List<SeatDto> allSeats = getSeatsByHall(hallId);

        // Фильтруем лучшие места (первые 3 ряда)
        List<SeatDto> bestSeats = allSeats.stream()
                .filter(seat -> seat.getRowNumber() != null && seat.getRowNumber() <= 3)
                .collect(Collectors.toList());

        return bestSeats;
    }

    // Получить места VIP категории
    public List<SeatDto> getVipSeats(Short hallId) {
        List<SeatDto> allSeats = getSeatsByHall(hallId);

        // Фильтруем VIP места (тип "VIP")
        List<SeatDto> vipSeats = allSeats.stream()
                .filter(seat -> "VIP".equalsIgnoreCase(seat.getSeatType()))
                .collect(Collectors.toList());

        return vipSeats;
    }
}