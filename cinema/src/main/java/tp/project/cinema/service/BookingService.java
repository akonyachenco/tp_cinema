package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.BookingDto;
import tp.project.cinema.dto.Mapping.BookingMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Booking;
import tp.project.cinema.model.BookingStatus;
import tp.project.cinema.model.Session;
import tp.project.cinema.model.User;
import tp.project.cinema.repository.BookingRepository;
import tp.project.cinema.repository.BookingStatusRepository;
import tp.project.cinema.repository.SessionRepository;
import tp.project.cinema.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final BookingMapping bookingMapping;

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найден"));
        return bookingMapping.toDto(booking);
    }

    public BookingDto createBooking(BookingDto bookingDto) {
        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + bookingDto.getUserId() + " не найден"));

        Session session = sessionRepository.findById(Math.toIntExact(bookingDto.getSessionId()))
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + bookingDto.getSessionId() + " не найден"));

        BookingStatus status = bookingStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new ResourceNotFoundException("Статус бронирования 'PENDING' не найден"));

        Booking booking = bookingMapping.toEntity(bookingDto);
        booking.setUser(user);
        booking.setSession(session);
        booking.setBooking_status(status);
        booking.setBooking_time(LocalDateTime.now());
        booking.setTotal_cost(BigDecimal.ZERO); // Рассчитывается при добавлении билетов

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapping.toDto(savedBooking);
    }

    public BookingDto updateBooking(Long id, BookingDto bookingDto) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найден"));

        BookingStatus status = bookingStatusRepository.findByStatusName(bookingDto.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Статус бронирования '" + bookingDto.getStatus() + "' не найден"));

        existingBooking.setBooking_status(status);

        Booking updatedBooking = bookingRepository.save(existingBooking);
        return bookingMapping.toDto(updatedBooking);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Бронирование с ID " + id + " не найден");
        }
        bookingRepository.deleteById(id);
    }

    public List<BookingDto> getBookingsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return bookingRepository.findByUserUserId(userId).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsBySession(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден");
        }

        return bookingRepository.findBySessionSessionId(sessionId).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найден"));

        BookingStatus confirmedStatus = bookingStatusRepository.findByStatusName("CONFIRMED")
                .orElseThrow(() -> new ResourceNotFoundException("Статус бронирования 'CONFIRMED' не найден"));

        booking.setBooking_status(confirmedStatus);

        Booking confirmedBooking = bookingRepository.save(booking);
        return bookingMapping.toDto(confirmedBooking);
    }

    public BookingDto cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найден"));

        BookingStatus cancelledStatus = bookingStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ResourceNotFoundException("Статус бронирования 'CANCELLED' не найден"));

        booking.setBooking_status(cancelledStatus);

        Booking cancelledBooking = bookingRepository.save(booking);
        return bookingMapping.toDto(cancelledBooking);
    }
}