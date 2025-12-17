package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserUserId(Long userId);

    List<Booking> findBySessionSessionId(Integer sessionId);

    List<Booking> findByBookingTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookingStatusStatusName(String statusName);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId AND b.bookingTime >= :startDate")
    List<Booking> findUserBookingsFromDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate);

    @Query("SELECT b FROM Booking b WHERE b.session.sessionId = :sessionId " +
            "AND b.bookingStatus.statusName != 'CANCELLED'")
    List<Booking> findActiveBookingsBySession(@Param("sessionId") Integer sessionId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.session.sessionId = :sessionId " +
            "AND b.bookingStatus.statusName = 'CONFIRMED'")
    long countConfirmedBookingsBySession(@Param("sessionId") Integer sessionId);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.bookingStatus.statusName = :status")
    List<Booking> findUserBookingsByStatus(
            @Param("userId") Long userId,
            @Param("status") String status);

    @Query("SELECT b FROM Booking b WHERE b.total_cost > :minAmount")
    List<Booking> findBookingsWithTotalCostGreaterThan(@Param("minAmount") Double minAmount);

    @Query("SELECT b FROM Booking b WHERE b.session.sessionId = :sessionId " +
            "AND b.user.userId = :userId")
    Optional<Booking> findBySessionAndUser(
            @Param("sessionId") Integer sessionId,
            @Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN b.ticketList t WHERE t.ticketId = :ticketId")
    Optional<Booking> findByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT SUM(b.total_cost) FROM Booking b WHERE b.bookingTime BETWEEN :start AND :end")
    Double getTotalRevenueForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT b.user.userId) FROM Booking b WHERE b.bookingTime BETWEEN :start AND :end")
    Long countUniqueUsersForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}