package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.service.SeatService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<SeatDto>> getSeatsByHall(@PathVariable Short hallId) {
        List<SeatDto> seats = seatService.getSeatsByHall(hallId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatDto> getSeatById(@PathVariable Integer id) {
        SeatDto seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("/session/{sessionId}/available")
    public ResponseEntity<List<SeatDto>> getAvailableSeatsForSession(@PathVariable Integer sessionId) {
        List<SeatDto> seats = seatService.getAvailableSeatsForSession(sessionId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/session/{sessionId}/booked")
    public ResponseEntity<List<SeatDto>> getBookedSeatsForSession(@PathVariable Integer sessionId) {
        List<SeatDto> seats = seatService.getBookedSeatsForSession(sessionId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/row/{rowNumber}/hall/{hallId}")
    public ResponseEntity<List<SeatDto>> getSeatsByRow(
            @PathVariable Short rowNumber,
            @PathVariable Short hallId) {
        List<SeatDto> seats = seatService.getSeatsByRow(rowNumber, hallId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/type/{seatType}")
    public ResponseEntity<List<SeatDto>> getSeatsByType(@PathVariable String seatType) {
        List<SeatDto> seats = seatService.getSeatsByType(seatType);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<SeatDto>> getSeatsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<SeatDto> seats = seatService.getSeatsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(seats);
    }
}