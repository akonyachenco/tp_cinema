package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.service.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable Long id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TicketDto>> getTicketsByBooking(@PathVariable Long bookingId) {
        List<TicketDto> tickets = ticketService.getTicketsByBooking(bookingId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDto>> getTicketsByUser(@PathVariable Long userId) {
        List<TicketDto> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<TicketDto>> getTicketsBySession(@PathVariable Integer sessionId) {
        List<TicketDto> tickets = ticketService.getTicketsBySession(sessionId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/code/{ticketCode}")
    public ResponseEntity<TicketDto> getTicketByCode(@PathVariable String ticketCode) {
        TicketDto ticket = ticketService.getTicketByCode(ticketCode);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/validate/{ticketCode}")
    public ResponseEntity<Boolean> validateTicket(@PathVariable String ticketCode) {
        boolean isValid = ticketService.validateTicket(ticketCode);
        return ResponseEntity.ok(isValid);
    }

    @PatchMapping("/{id}/use")
    public ResponseEntity<TicketDto> markTicketAsUsed(@PathVariable Long id) {
        TicketDto ticket = ticketService.markTicketAsUsed(id);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<TicketDto>> getTicketsByHall(@PathVariable Short hallId) {
        List<TicketDto> tickets = ticketService.getTicketsByHall(hallId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TicketDto>> getTicketsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TicketDto> tickets = ticketService.getTicketsByDateRange(startDate, endDate);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketDto>> getTicketsByStatus(@PathVariable String status) {
        List<TicketDto> tickets = ticketService.getTicketsByBookingStatus(status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<TicketDto>> getUpcomingTicketsByUser(@PathVariable Long userId) {
        List<TicketDto> tickets = ticketService.getUpcomingTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        BigDecimal revenue = ticketService.getTotalRevenueForPeriod(start, end);

        Map<String, BigDecimal> response = new HashMap<>();
        response.put("revenue", revenue);

        return ResponseEntity.ok(response);
    }
}