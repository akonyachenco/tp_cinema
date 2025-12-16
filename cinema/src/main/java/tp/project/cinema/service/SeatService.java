package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.dto.Mapping.SeatMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Seat;
import tp.project.cinema.repository.SeatRepository;
import tp.project.cinema.repository.SessionRepository;
import tp.project.cinema.repository.TicketRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {

    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final SeatMapping seatMapping;

    public List<SeatDto> getSeatsByHall(Short hallId) {
        if (!seatRepository.existsByHallHallId(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        return seatRepository.findByHallHallId(hallId).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    public SeatDto getSeatById(Integer id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Место с ID " + id + " не найден"));
        return seatMapping.toDto(seat);
    }

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

    public List<SeatDto> getSeatsByRow(Short rowNumber, Short hallId) {
        if (!seatRepository.existsByHallHallId(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        return seatRepository.findByRowNumberAndHallHallId(rowNumber, hallId).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SeatDto> getSeatsByType(String seatType) {
        return seatRepository.findBySeatTypeTypeName(seatType).stream()
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SeatDto> getSeatsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        // Эта логика зависит от структуры цен, пока возвращаем все места
        // В реальном приложении нужно рассчитать цену для каждого места
        return seatRepository.findAll().stream()
                .filter(seat -> {
                    BigDecimal price = seat.getHall().getBase_price()
                            .multiply(seat.getSeat_type().getPrice_multiplier());
                    return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
                })
                .map(seatMapping::toDto)
                .collect(Collectors.toList());
    }
}