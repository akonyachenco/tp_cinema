package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Hall;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Short> {

    Optional<Hall> findByHallName(String hallName);

    List<Hall> findByHallNameContainingIgnoreCase(String hallName);

    List<Hall> findByStatus(String status);

    List<Hall> findByHallTypeTypeName(String typeName);

    @Query("SELECT h FROM Hall h WHERE h.basePrice <= :maxPrice")
    List<Hall> findByBasePriceLessThanEqual(@Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT h FROM Hall h WHERE h.basePrice >= :minPrice")
    List<Hall> findByBasePriceGreaterThanEqual(@Param("minPrice") BigDecimal minPrice);

    @Query("SELECT h FROM Hall h WHERE h.rowsCount >= :minRows AND h.seatsPerRow >= :minSeatsPerRow")
    List<Hall> findByMinCapacity(
            @Param("minRows") Short minRows,
            @Param("minSeatsPerRow") Short minSeatsPerRow);

    @Query("SELECT h FROM Hall h WHERE (h.rowsCount * h.seatsPerRow) >= :minCapacity")
    List<Hall> findByTotalCapacityGreaterThanEqual(@Param("minCapacity") Integer minCapacity);

    @Query("SELECT h FROM Hall h WHERE h.hallId IN " +
            "(SELECT s.hall.hallId FROM Session s WHERE s.sessionId = :sessionId)")
    Optional<Hall> findHallBySessionId(@Param("sessionId") Integer sessionId);

    @Query("SELECT h FROM Hall h WHERE h.status = 'AVAILABLE' " +
            "AND (h.rowsCount * h.seatsPerRow) >= :requiredCapacity")
    List<Hall> findAvailableHallsWithCapacity(@Param("requiredCapacity") Integer requiredCapacity);

    @Query("SELECT h FROM Hall h WHERE h.hallId NOT IN " +
            "(SELECT s.hall.hallId FROM Session s WHERE s.dateTime = :dateTime)")
    List<Hall> findAvailableHallsAtTime(@Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT h FROM Hall h WHERE h.hallId IN " +
            "(SELECT DISTINCT s.hall.hallId FROM Session s WHERE s.film.filmId = :filmId)")
    List<Hall> findHallsShowingFilm(@Param("filmId") Long filmId);

    @Query("SELECT SUM(h.rowsCount * h.seatsPerRow) FROM Hall h")
    Integer getTotalSeatingCapacity();

    @Query("SELECT h.hallType.typeName, COUNT(h) FROM Hall h GROUP BY h.hallType.typeName")
    List<Object[]> countHallsByType();
}