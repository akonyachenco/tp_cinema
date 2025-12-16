package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.HallDto;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.service.HallService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @GetMapping
    public ResponseEntity<List<HallDto>> getAllHalls() {
        List<HallDto> halls = hallService.getAllHalls();
        return ResponseEntity.ok(halls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDto> getHallById(@PathVariable Short id) {
        HallDto hall = hallService.getHallById(id);
        return ResponseEntity.ok(hall);
    }

    @GetMapping("/available")
    public ResponseEntity<List<HallDto>> getAvailableHalls() {
        List<HallDto> halls = hallService.getAvailableHalls();
        return ResponseEntity.ok(halls);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<HallDto>> getHallsByType(@PathVariable String type) {
        List<HallDto> halls = hallService.getHallsByType(type);
        return ResponseEntity.ok(halls);
    }

    @GetMapping("/{hallId}/seats")
    public ResponseEntity<List<SeatDto>> getHallSeats(@PathVariable Short hallId) {
        List<SeatDto> seats = hallService.getHallSeats(hallId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{hallId}/layout")
    public ResponseEntity<HallDto> getHallWithLayout(@PathVariable Short hallId) {
        HallDto hall = hallService.getHallWithLayout(hallId);
        return ResponseEntity.ok(hall);
    }

    @PostMapping
    public ResponseEntity<HallDto> createHall(@Valid @RequestBody HallDto hallDto) {
        // Устанавливаем статус по умолчанию, если не указан
        if (hallDto.getStatus() == null || hallDto.getStatus().isEmpty()) {
            hallDto.setStatus("AVAILABLE");
        }

        HallDto createdHall = hallService.createHall(hallDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHall);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallDto> updateHall(
            @PathVariable Short id,
            @Valid @RequestBody HallDto hallDto) {
        HallDto updatedHall = hallService.updateHall(id, hallDto);
        return ResponseEntity.ok(updatedHall);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HallDto> updateHallStatus(
            @PathVariable Short id,
            @RequestParam String status) {
        HallDto updatedHall = hallService.updateHallStatus(id, status);
        return ResponseEntity.ok(updatedHall);
    }

    @GetMapping("/capacity/available")
    public ResponseEntity<List<HallDto>> getAvailableHallsWithCapacity(
            @RequestParam(required = false, defaultValue = "1") Integer capacity) {
        List<HallDto> halls = hallService.findAvailableHallsWithCapacity(capacity);
        return ResponseEntity.ok(halls);
    }

    @GetMapping("/total-capacity")
    public ResponseEntity<Integer> getTotalSeatingCapacity() {
        Integer capacity = hallService.getTotalSeatingCapacity();
        return ResponseEntity.ok(capacity);
    }
}