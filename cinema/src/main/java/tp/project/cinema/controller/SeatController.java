package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.service.SeatService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // Получить места зала
    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<SeatDto>> getSeatsByHall(@PathVariable Short hallId) {
        List<SeatDto> seats = seatService.getSeatsByHall(hallId);
        return ResponseEntity.ok(seats);
    }

    // Получить место по ID
    @GetMapping("/{id}")
    public ResponseEntity<SeatDto> getSeatById(@PathVariable Integer id) {
        SeatDto seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    // Получить доступные места для сеанса
    @GetMapping("/available")
    public ResponseEntity<List<SeatDto>> getAvailableSeatsForSession(
            @RequestParam Integer sessionId) {
        List<SeatDto> seats = seatService.getAvailableSeatsForSession(sessionId);
        return ResponseEntity.ok(seats);
    }

    // Получить занятые места сеанса
    @GetMapping("/booked")
    public ResponseEntity<List<SeatDto>> getBookedSeatsForSession(
            @RequestParam Integer sessionId) {
        List<SeatDto> seats = seatService.getBookedSeatsForSession(sessionId);
        return ResponseEntity.ok(seats);
    }

    // Получить места по ряду
    @GetMapping("/row/{rowNumber}")
    public ResponseEntity<List<SeatDto>> getSeatsByRow(
            @PathVariable Short rowNumber,
            @RequestParam Short hallId) {
        List<SeatDto> seats = seatService.getSeatsByRow(rowNumber, hallId);
        return ResponseEntity.ok(seats);
    }

    // Получить места по типу
    @GetMapping("/type/{seatType}")
    public ResponseEntity<List<SeatDto>> getSeatsByType(@PathVariable String seatType) {
        List<SeatDto> seats = seatService.getSeatsByType(seatType);
        return ResponseEntity.ok(seats);
    }

    // Получить места по диапазону цен
    @GetMapping("/price-range")
    public ResponseEntity<List<SeatDto>> getSeatsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<SeatDto> seats = seatService.getSeatsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(seats);
    }

    // Получить схему зала (для фронтенда)
    @GetMapping("/hall/{hallId}/layout")
    public ResponseEntity<Map<String, Object>> getHallLayout(@PathVariable Short hallId) {
        Map<String, Object> layout = seatService.getHallLayout(hallId);
        return ResponseEntity.ok(layout);
    }

    // Получить все места для бронирования сеанса
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSeatsForSession(
            @PathVariable Integer sessionId) {
        Map<String, Object> seatsInfo = seatService.getSeatsForBooking(sessionId);
        return ResponseEntity.ok(seatsInfo);
    }

    // Проверить доступность конкретного места на сеансе
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Boolean>> checkSeatAvailability(
            @RequestParam Integer seatId,
            @RequestParam Integer sessionId) {
        // Логика проверки доступности
        List<SeatDto> bookedSeats = seatService.getBookedSeatsForSession(sessionId);
        boolean isAvailable = bookedSeats.stream()
                .noneMatch(seat -> seat.getSeatId().equals(seatId));

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }

    // Получить количество свободных мест на сеансе
    @GetMapping("/session/{sessionId}/available-count")
    public ResponseEntity<Map<String, Integer>> getAvailableSeatsCount(
            @PathVariable Integer sessionId) {
        List<SeatDto> availableSeats = seatService.getAvailableSeatsForSession(sessionId);

        Map<String, Integer> response = new HashMap<>();
        response.put("availableCount", availableSeats.size());

        return ResponseEntity.ok(response);
    }

    // Получить схему зала с указанием занятых мест для сеанса
    @GetMapping("/session/{sessionId}/layout")
    public ResponseEntity<Map<String, Object>> getSessionHallLayout(
            @PathVariable Integer sessionId) {
        Map<String, Object> seatsInfo = seatService.getSeatsForBooking(sessionId);

        // Извлекаем нужные данные
        Map<String, Object> layout = new HashMap<>();
        layout.put("hallId", seatsInfo.get("hallId"));
        layout.put("hallName", seatsInfo.get("hallName"));
        layout.put("sessionId", sessionId);
        layout.put("filmTitle", seatsInfo.get("filmTitle"));
        layout.put("sessionDateTime", seatsInfo.get("sessionDateTime"));
        layout.put("seats", seatsInfo.get("allSeats"));

        return ResponseEntity.ok(layout);
    }

    // Получить лучшие места в зале (первые ряды)
    @GetMapping("/hall/{hallId}/best")
    public ResponseEntity<List<SeatDto>> getBestSeats(@PathVariable Short hallId) {
        List<SeatDto> allSeats = seatService.getSeatsByHall(hallId);

        // Фильтруем лучшие места (первые 3 ряда)
        List<SeatDto> bestSeats = allSeats.stream()
                .filter(seat -> seat.getRowNumber() <= 3)
                .toList();

        return ResponseEntity.ok(bestSeats);
    }

    // Получить места VIP категории
    @GetMapping("/hall/{hallId}/vip")
    public ResponseEntity<List<SeatDto>> getVipSeats(@PathVariable Short hallId) {
        List<SeatDto> allSeats = seatService.getSeatsByHall(hallId);

        // Фильтруем VIP места (тип "VIP")
        List<SeatDto> vipSeats = allSeats.stream()
                .filter(seat -> "VIP".equalsIgnoreCase(seat.getSeatType()))
                .toList();

        return ResponseEntity.ok(vipSeats);
    }

    // Получить информацию о конкретном месте
    @GetMapping("/{seatId}/info")
    public ResponseEntity<Map<String, Object>> getSeatInfo(
            @PathVariable Integer seatId,
            @RequestParam(required = false) Integer sessionId) {
        SeatDto seat = seatService.getSeatById(seatId);

        Map<String, Object> info = new HashMap<>();
        info.put("seatId", seat.getSeatId());
        info.put("rowNumber", seat.getRowNumber());
        info.put("seatNumber", seat.getSeatNumber());
        info.put("seatType", seat.getSeatType());
        info.put("priceMultiplier", seat.getPriceMultiplier());
        info.put("hallId", seat.getHallId());
        info.put("hallName", seat.getHallName());
        info.put("basePrice", seat.getBasePrice());

        // Рассчитываем финальную цену
        BigDecimal finalPrice = seat.getBasePrice().multiply(seat.getPriceMultiplier());
        info.put("finalPrice", finalPrice);

        // Если передан sessionId, проверяем доступность
        if (sessionId != null) {
            List<SeatDto> bookedSeats = seatService.getBookedSeatsForSession(sessionId);
            boolean isAvailable = bookedSeats.stream()
                    .noneMatch(s -> s.getSeatId().equals(seatId));
            info.put("available", isAvailable);
        }

        return ResponseEntity.ok(info);
    }
}