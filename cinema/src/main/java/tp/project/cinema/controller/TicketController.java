package tp.project.cinema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.service.TicketService;

import java.util.List;

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
}