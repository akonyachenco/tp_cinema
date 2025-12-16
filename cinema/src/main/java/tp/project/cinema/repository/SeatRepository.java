package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Hall;
import tp.project.cinema.model.Seat;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    List<Seat> findByHallHallId(Short hallId);

    List<Seat> findByRowNumberAndHallHallId(Short rowNumber, Short hallId);

    Optional<Seat> findByRowNumberAndSeatNumberAndHallHallId(
            Short rowNumber, Short seatNumber, Short hallId);

    List<Seat> findBySeatTypeTypeName(String seatTypeName);

    boolean existsByHallHallId(Short hallId);

    @Query("SELECT s FROM Seat s WHERE s.hall.hallId = :hallId " +
            "AND s.seatId NOT IN " +
            "(SELECT t.seat.seatId FROM Ticket t JOIN t.booking b " +
            "WHERE b.session.sessionId = :sessionId AND b.bookingStatus.statusName != 'CANCELLED')")
    List<Seat> findAvailableSeatsForSession(
            @Param("hallId") Short hallId,
            @Param("sessionId") Integer sessionId);

    @Query("SELECT s FROM Seat s WHERE s.hall.hallId = :hallId " +
            "AND s.seatId IN " +
            "(SELECT t.seat.seatId FROM Ticket t JOIN t.booking b " +
            "WHERE b.session.sessionId = :sessionId AND b.bookingStatus.statusName = 'CONFIRMED')")
    List<Seat> findBookedSeatsForSession(
            @Param("hallId") Short hallId,
            @Param("sessionId") Integer sessionId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.hallId = :hallId")
    Integer countSeatsByHall(@Param("hallId") Short hallId);

    @Query("SELECT s FROM Seat s WHERE s.hall.hallId = :hallId " +
            "AND s.seatType.typeName = :seatType")
    List<Seat> findByHallAndSeatType(
            @Param("hallId") Short hallId,
            @Param("seatType") String seatType);

    @Query("SELECT s FROM Seat s WHERE s.hall.hallId = :hallId " +
            "ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByHallOrdered(@Param("hallId") Short hallId);

    @Query("SELECT s.rowNumber, COUNT(s) FROM Seat s WHERE s.hall.hallId = :hallId GROUP BY s.rowNumber")
    List<Object[]> countSeatsPerRowByHall(@Param("hallId") Short hallId);

    @Query("SELECT s.seatType.typeName, COUNT(s) FROM Seat s WHERE s.hall.hallId = :hallId " +
            "GROUP BY s.seatType.typeName")
    List<Object[]> countSeatsByTypeInHall(@Param("hallId") Short hallId);

    @Query("SELECT DISTINCT s.hall FROM Seat s WHERE s.seatType.typeName = :seatType")
    List<Hall> findHallsWithSeatType(@Param("seatType") String seatType);

    @Query("SELECT s FROM Seat s WHERE s.hall.hallId = :hallId " +
            "AND (s.rowNumber * 100 + s.seatNumber) BETWEEN :start AND :end")
    List<Seat> findSeatsInRange(
            @Param("hallId") Short hallId,
            @Param("start") Integer start,
            @Param("end") Integer end);
}