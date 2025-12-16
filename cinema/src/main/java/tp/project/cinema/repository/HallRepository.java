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

    @Query("SELECT h FROM Hall h WHERE h.base_price <= :maxPrice")
    List<Hall> findByBasePriceLessThanEqual(@Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT h FROM Hall h WHERE h.base_price >= :minPrice")
    List<Hall> findByBasePriceGreaterThanEqual(@Param("minPrice") BigDecimal minPrice);

    @Query("SELECT h FROM Hall h WHERE h.rows_count >= :minRows AND h.seats_per_row >= :minSeatsPerRow")
    List<Hall> findByMinCapacity(
            @Param("minRows") Short minRows,
            @Param("minSeatsPerRow") Short minSeatsPerRow);

    @Query("SELECT h FROM Hall h WHERE (h.rows_count * h.seats_per_row) >= :minCapacity")
    List<Hall> findByTotalCapacityGreaterThanEqual(@Param("minCapacity") Integer minCapacity);

    @Query("SELECT h FROM Hall h WHERE h.hall_id IN " +
            "(SELECT s.hall.hall_id FROM Session s WHERE s.session_id = :sessionId)")
    Optional<Hall> findHallBySessionId(@Param("sessionId") Integer sessionId);

    @Query("SELECT h FROM Hall h WHERE h.status = 'AVAILABLE' " +
            "AND (h.rows_count * h.seats_per_row) >= :requiredCapacity")
    List<Hall> findAvailableHallsWithCapacity(@Param("requiredCapacity") Integer requiredCapacity);

    @Query("SELECT h FROM Hall h WHERE h.hall_id NOT IN " +
            "(SELECT s.hall.hall_id FROM Session s WHERE s.date_time = :dateTime)")
    List<Hall> findAvailableHallsAtTime(@Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT h FROM Hall h WHERE h.hall_id IN " +
            "(SELECT DISTINCT s.hall.hall_id FROM Session s WHERE s.film.film_id = :filmId)")
    List<Hall> findHallsShowingFilm(@Param("filmId") Long filmId);

    @Query("SELECT SUM(h.rows_count * h.seats_per_row) FROM Hall h")
    Integer getTotalSeatingCapacity();

    @Query("SELECT h.hall_type.type_name, COUNT(h) FROM Hall h GROUP BY h.hall_type.type_name")
    List<Object[]> countHallsByType();
}