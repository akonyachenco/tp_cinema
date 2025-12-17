package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.BookingDto;
import tp.project.cinema.dto.TicketDto;
import tp.project.cinema.dto.Mapping.BookingMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.*;
import tp.project.cinema.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final BookingMapping bookingMapping;

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найдено"));
        return bookingMapping.toDto(booking);
    }

    public BookingDto createBooking(BookingDto bookingDto) {
        // 1. Валидация входных данных
        validateBookingRequest(bookingDto);

        // 2. Поиск пользователя
        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + bookingDto.getUserId() + " не найден"));

        // 3. Поиск сеанса
        Session session = sessionRepository.findById(Math.toIntExact(bookingDto.getSessionId()))
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс с ID " + bookingDto.getSessionId() + " не найден"));

        // 4. Проверка, что сеанс еще не начался
        if (session.getDate_time().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Сеанс уже начался или завершился");
        }

        // 5. Проверка, что сеанс не отменен
        if ("CANCELLED".equals(session.getStatus())) {
            throw new IllegalArgumentException("Сеанс отменен");
        }

        // 6. Получение статуса бронирования (по умолчанию PENDING)
        BookingStatus status = bookingStatusRepository.findByStatusName(
                bookingDto.getStatus() != null ? bookingDto.getStatus() : "PENDING"
        ).orElseGet(() -> {
            BookingStatus newStatus = new BookingStatus();
            newStatus.setStatus_name("PENDING");
            return bookingStatusRepository.save(newStatus);
        });

        // 7. Создание бронирования
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSession(session);
        booking.setBooking_status(status);
        booking.setBooking_time(LocalDateTime.now());
        booking.setTotal_cost(BigDecimal.ZERO);
        booking.setTicket_list(new ArrayList<>());

        // 8. Сохранение бронирования (нужно сделать до создания билетов)
        Booking savedBooking = bookingRepository.save(booking);

        // 9. Обработка билетов
        BigDecimal totalCost = BigDecimal.ZERO;
        List<Ticket> createdTickets = new ArrayList<>();

        if (bookingDto.getTicketList() != null && !bookingDto.getTicketList().isEmpty()) {
            // Проверяем, не превышает ли количество выбранных мест доступные
            int availableSeatsCount = seatRepository.findAvailableSeatsForSession(
                    session.getHall().getHall_id(),
                    session.getSession_id()
            ).size();

            if (bookingDto.getTicketList().size() > availableSeatsCount) {
                throw new IllegalArgumentException("Недостаточно свободных мест. Доступно: " + availableSeatsCount);
            }

            // Создаем билеты для каждого выбранного места
            for (TicketDto ticketDto : bookingDto.getTicketList()) {
                Seat seat = seatRepository.findById(ticketDto.getSeatId())
                        .orElseThrow(() -> new ResourceNotFoundException("Место с ID " + ticketDto.getSeatId() + " не найдено"));

                // Проверяем, что место принадлежит правильному залу
                if (seat.getHall().getHall_id() != (session.getHall().getHall_id())) {
                    throw new IllegalArgumentException("Место " + ticketDto.getSeatId() + " не принадлежит залу этого сеанса");
                }

                // Проверяем, свободно ли место на этот сеанс
                boolean isSeatAvailable = ticketRepository.findBySeatAndSession(
                        ticketDto.getSeatId(),
                        session.getSession_id()
                ).isEmpty();

                if (!isSeatAvailable) {
                    throw new IllegalArgumentException("Место " + ticketDto.getSeatId() + " уже занято на этот сеанс");
                }

                // Создаем билет
                Ticket ticket = createTicket(ticketDto, seat, savedBooking);

                // Рассчитываем цену
                BigDecimal ticketPrice = calculateTicketPrice(ticketDto, seat);
                ticket.setPrice(ticketPrice);
                totalCost = totalCost.add(ticketPrice);

                // Генерируем уникальный код билета
                String ticketCode = generateTicketCode();
                ticket.setTicket_code(ticketCode);

                // Сохраняем билет
                Ticket savedTicket = ticketRepository.save(ticket);
                createdTickets.add(savedTicket);
                savedBooking.getTicket_list().add(savedTicket);
            }
        } else {
            throw new IllegalArgumentException("Не выбрано ни одного места для бронирования");
        }

        // 10. Обновляем общую стоимость бронирования
        savedBooking.setTotal_cost(totalCost);

        // 11. Обновляем список билетов
        savedBooking.setTicket_list(createdTickets);

        // 12. Сохраняем обновленное бронирование
        Booking finalBooking = bookingRepository.save(savedBooking);

        // 13. Возвращаем DTO
        return bookingMapping.toDto(finalBooking);
    }

    private void validateBookingRequest(BookingDto bookingDto) {
        if (bookingDto.getUserId() == null) {
            throw new IllegalArgumentException("ID пользователя обязателен");
        }
        if (bookingDto.getSessionId() == null) {
            throw new IllegalArgumentException("ID сеанса обязателен");
        }
        if (bookingDto.getTicketList() == null || bookingDto.getTicketList().isEmpty()) {
            throw new IllegalArgumentException("Должно быть выбрано хотя бы одно место");
        }

        // Проверка на дубликаты мест
        Set<Integer> seatIds = new HashSet<>();
        for (TicketDto ticketDto : bookingDto.getTicketList()) {
            if (ticketDto.getSeatId() == null) {
                throw new IllegalArgumentException("ID места обязательно для каждого билета");
            }
            if (!seatIds.add(ticketDto.getSeatId())) {
                throw new IllegalArgumentException("Место " + ticketDto.getSeatId() + " выбрано более одного раза");
            }
        }
    }

    private Ticket createTicket(TicketDto ticketDto, Seat seat, Booking booking) {
        Ticket ticket = new Ticket();
        ticket.setSeat(seat);
        ticket.setBooking(booking);
        ticket.setCreation_date(LocalDateTime.now());

        return ticket;
    }

    private BigDecimal calculateTicketPrice(TicketDto ticketDto, Seat seat) {
        // Если цена указана в DTO, используем ее
        if (ticketDto.getPrice() != null) {
            return ticketDto.getPrice();
        }

        // Иначе рассчитываем цену: базовая цена зала * множитель типа места
        BigDecimal basePrice = seat.getHall().getBase_price();
        BigDecimal multiplier = seat.getSeat_type().getPrice_multiplier();

        return basePrice.multiply(multiplier);
    }

    private String generateTicketCode() {
        // Генерация уникального кода билета: TK-XXXXX-YYYYY
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000000);
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return String.format("TK-%s-%s", timestamp, random);
    }

    public BookingDto updateBooking(Long id, BookingDto bookingDto) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найдено"));

        BookingStatus status = bookingStatusRepository.findByStatusName(bookingDto.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Статус бронирования '" + bookingDto.getStatus() + "' не найден"));

        existingBooking.setBooking_status(status);

        Booking updatedBooking = bookingRepository.save(existingBooking);
        return bookingMapping.toDto(updatedBooking);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Бронирование с ID " + id + " не найдено");
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

    public List<BookingDto> getBookingsByStatus(String status) {
        return bookingRepository.findByBookingStatusStatusName(status).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найдено"));

        BookingStatus confirmedStatus = bookingStatusRepository.findByStatusName("CONFIRMED")
                .orElseGet(() -> {
                    BookingStatus newStatus = new BookingStatus();
                    newStatus.setStatus_name("CONFIRMED");
                    return bookingStatusRepository.save(newStatus);
                });

        booking.setBooking_status(confirmedStatus);

        Booking confirmedBooking = bookingRepository.save(booking);
        return bookingMapping.toDto(confirmedBooking);
    }

    public BookingDto cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с ID " + id + " не найдено"));

        BookingStatus cancelledStatus = bookingStatusRepository.findByStatusName("CANCELLED")
                .orElseGet(() -> {
                    BookingStatus newStatus = new BookingStatus();
                    newStatus.setStatus_name("CANCELLED");
                    return bookingStatusRepository.save(newStatus);
                });

        booking.setBooking_status(cancelledStatus);

        Booking cancelledBooking = bookingRepository.save(booking);
        return bookingMapping.toDto(cancelledBooking);
    }

    public List<BookingDto> getActiveBookingsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return bookingRepository.findUserBookingsByStatus(userId, "CONFIRMED").stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getActiveBookingsBySession(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден");
        }

        return bookingRepository.findActiveBookingsBySession(sessionId).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public Double getTotalRevenue(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        Double revenue = bookingRepository.getTotalRevenueForPeriod(start, end);
        return revenue != null ? revenue : 0.0;
    }

    // Дополнительные методы

    public List<BookingDto> getBookingsByDateRange(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByBookingTimeBetween(start, end).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsFromDate(Long userId, LocalDateTime startDate) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return bookingRepository.findUserBookingsFromDate(userId, startDate).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public long countConfirmedBookingsBySession(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Сеанс с ID " + sessionId + " не найден");
        }

        return bookingRepository.countConfirmedBookingsBySession(sessionId);
    }

    public List<BookingDto> getBookingsWithTotalCostGreaterThan(Double minAmount) {
        return bookingRepository.findBookingsWithTotalCostGreaterThan(minAmount).stream()
                .map(bookingMapping::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingBySessionAndUser(Integer sessionId, Long userId) {
        Booking booking = bookingRepository.findBySessionAndUser(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Бронирование для сеанса %d и пользователя %d не найдено", sessionId, userId)));

        return bookingMapping.toDto(booking);
    }

    public BookingDto getBookingByTicketId(Long ticketId) {
        Booking booking = bookingRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Бронирование для билета %d не найдено", ticketId)));

        return bookingMapping.toDto(booking);
    }

    public Long countUniqueUsersForPeriod(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        Long count = bookingRepository.countUniqueUsersForPeriod(start, end);
        return count != null ? count : 0L;
    }

    public Map<String, Object> getBookingStatistics(LocalDateTime start, LocalDateTime end) {
        Double revenue = bookingRepository.getTotalRevenueForPeriod(start, end);
        Long uniqueUsers = bookingRepository.countUniqueUsersForPeriod(start, end);
        List<BookingDto> recentBookings = getBookingsByDateRange(start, end);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("revenue", revenue != null ? revenue : 0.0);
        statistics.put("uniqueUsers", uniqueUsers != null ? uniqueUsers : 0);
        statistics.put("totalBookings", recentBookings.size());
        statistics.put("averageBookingValue", revenue != null && recentBookings.size() > 0
                ? revenue / recentBookings.size() : 0);

        return statistics;
    }
}