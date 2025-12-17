package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByBookingBookingId(Long bookingId);

    @Query("SELECT t FROM Ticket t WHERE t.booking.user.userId = :userId")
    List<Ticket> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.seat.hall.hallId = :hallId")
    List<Ticket> findByHallId(@Param("hallId") Short hallId);

    @Query("SELECT t FROM Ticket t WHERE t.booking.session.sessionId = :sessionId")
    List<Ticket> findBySessionId(@Param("sessionId") Integer sessionId);

    @Query("SELECT t FROM Ticket t WHERE t.creation_date BETWEEN :startDate AND :endDate")
    List<Ticket> findByCreationDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Ticket t WHERE t.booking.bookingStatus.statusName = :status")
    List<Ticket> findByBookingStatus(@Param("status") String status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.booking.session.sessionId = :sessionId")
    long countTicketsBySession(@Param("sessionId") Integer sessionId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.seat.hall.hallId = :hallId")
    long countTicketsByHall(@Param("hallId") Short hallId);

    @Query("SELECT t FROM Ticket t WHERE t.seat.seatId = :seatId " +
            "AND t.booking.session.sessionId = :sessionId")
    Optional<Ticket> findBySeatAndSession(
            @Param("seatId") Integer seatId,
            @Param("sessionId") Integer sessionId);

    @Query("SELECT t FROM Ticket t WHERE t.booking.user.userId = :userId " +
            "AND t.booking.session.date_time >= CURRENT_DATE")
    List<Ticket> findUpcomingTicketsByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.creation_date BETWEEN :start AND :end")
    BigDecimal getTotalRevenueForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t.seat.hall.hallName, COUNT(t) FROM Ticket t " +
            "WHERE t.creation_date BETWEEN :start AND :end " +
            "GROUP BY t.seat.hall.hallName")
    List<Object[]> countTicketsByHallForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    boolean existsByTicketCode(String ticketCode);
}