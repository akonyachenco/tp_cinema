package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.dto.Mapping.TicketMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Ticket;
import tp.project.cinema.repository.TicketRepository;
import tp.project.cinema.repository.BookingRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final TicketMapping ticketMapping;

    public TicketDto getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Билет с ID " + id + " не найден"));
        return ticketMapping.toDto(ticket);
    }

    public List<TicketDto> getTicketsByBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Бронирование с ID " + bookingId + " не найден");
        }

        return ticketRepository.findByBookingBookingId(bookingId).stream()
                .map(ticketMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<TicketDto> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(ticketMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<TicketDto> getTicketsBySession(Integer sessionId) {
        return ticketRepository.findBySessionId(sessionId).stream()
                .map(ticketMapping::toDto)
                .collect(Collectors.toList());
    }

    public TicketDto getTicketByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Билет с кодом " + ticketCode + " не найден"));
        return ticketMapping.toDto(ticket);
    }

    public Boolean validateTicket(String ticketCode) {
        return ticketRepository.existsByTicketCode(ticketCode);
    }

    public TicketDto markTicketAsUsed(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Билет с ID " + id + " не найден"));

        // Здесь должна быть логика отметки билета как использованного
        // Например, обновление статуса в связанном бронировании

        return ticketMapping.toDto(ticket);
    }

    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Билет с ID " + id + " не найден");
        }
        ticketRepository.deleteById(id);
    }
}