package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.BookingDto;
import tp.project.cinema.service.BookingService;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        BookingDto booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingDto bookingDto) {
        // Устанавливаем статус по умолчанию, если не указан
        if (bookingDto.getStatus() == null || bookingDto.getStatus().isEmpty()) {
            bookingDto.setStatus("PENDING");
        }

        BookingDto createdBooking = bookingService.createBooking(bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDto> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingDto bookingDto) {
        BookingDto updatedBooking = bookingService.updateBooking(id, bookingDto);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDto>> getBookingsByUser(@PathVariable Long userId) {
        List<BookingDto> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<BookingDto>> getBookingsBySession(@PathVariable Integer sessionId) {
        List<BookingDto> bookings = bookingService.getBookingsBySession(sessionId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingDto>> getBookingsByStatus(@PathVariable String status) {
        List<BookingDto> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingDto> confirmBooking(@PathVariable Long id) {
        BookingDto booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(booking);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id) {
        BookingDto booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<BookingDto>> getActiveBookingsByUser(@PathVariable Long userId) {
        List<BookingDto> bookings = bookingService.getActiveBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/session/{sessionId}/active")
    public ResponseEntity<List<BookingDto>> getActiveBookingsBySession(@PathVariable Integer sessionId) {
        List<BookingDto> bookings = bookingService.getActiveBookingsBySession(sessionId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) {
        Double revenue = bookingService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<BookingDto>> getBookingHistory(@PathVariable Long userId) {
        List<BookingDto> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    // Дополнительные эндпоинты

    @GetMapping("/date-range")
    public ResponseEntity<List<BookingDto>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<BookingDto> bookings = bookingService.getBookingsByDateRange(start, end);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}/from-date")
    public ResponseEntity<List<BookingDto>> getBookingsFromDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        List<BookingDto> bookings = bookingService.getBookingsFromDate(userId, startDate);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/session/{sessionId}/confirmed-count")
    public ResponseEntity<Map<String, Long>> countConfirmedBookingsBySession(@PathVariable Integer sessionId) {
        long count = bookingService.countConfirmedBookingsBySession(sessionId);
        Map<String, Long> response = new HashMap<>();
        response.put("confirmedCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cost-greater-than")
    public ResponseEntity<List<BookingDto>> getBookingsWithTotalCostGreaterThan(
            @RequestParam Double minAmount) {
        List<BookingDto> bookings = bookingService.getBookingsWithTotalCostGreaterThan(minAmount);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/find-by-session-user")
    public ResponseEntity<BookingDto> getBookingBySessionAndUser(
            @RequestParam Integer sessionId,
            @RequestParam Long userId) {
        BookingDto booking = bookingService.getBookingBySessionAndUser(sessionId, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/find-by-ticket/{ticketId}")
    public ResponseEntity<BookingDto> getBookingByTicketId(@PathVariable Long ticketId) {
        BookingDto booking = bookingService.getBookingByTicketId(ticketId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/unique-users")
    public ResponseEntity<Map<String, Long>> countUniqueUsersForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) {
        Long count = bookingService.countUniqueUsersForPeriod(startDate, endDate);
        Map<String, Long> response = new HashMap<>();
        response.put("uniqueUsers", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Map<String, Object> statistics = bookingService.getBookingStatistics(start, end);
        return ResponseEntity.ok(statistics);
    }
}