package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Film;
import tp.project.cinema.model.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    List<Session> findByFilmFilmId(Long filmId);

    List<Session> findByHallHallId(Short hallId);

    List<Session> findByDateTimeAfter(LocalDateTime dateTime);

    List<Session> findByDateTimeBefore(LocalDateTime dateTime);

    List<Session> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Session> findByStatus(String status);

    @Query("SELECT s FROM Session s WHERE s.film.filmId = :filmId AND s.dateTime >= CURRENT_DATE")
    List<Session> findUpcomingSessionsByFilm(@Param("filmId") Long filmId);

    @Query("SELECT s FROM Session s WHERE s.hall.hallId = :hallId AND s.dateTime >= :startDate AND s.dateTime < :endDate")
    List<Session> findSessionsByHallAndDateRange(
            @Param("hallId") Short hallId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Session s WHERE s.dateTime >= :date ORDER BY s.dateTime ASC")
    List<Session> findSessionsFromDate(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Session s WHERE s.hall.hallId = :hallId AND s.dateTime = :dateTime")
    Optional<Session> findByHallAndDateTime(
            @Param("hallId") Short hallId,
            @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.film.filmId = :filmId")
    long countByFilmId(@Param("filmId") Long filmId);

    @Query("SELECT s FROM Session s WHERE s.hall.hallId = :hallId " +
            "AND s.dateTime BETWEEN :startTime AND :endTime " +
            "AND s.status != 'Отменен'")
    List<Session> findConflictingSessions(
            @Param("hallId") Short hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);


    @Query("SELECT s FROM Session s WHERE s.hall.hallId = :hallId " +
            "AND s.dateTime < :startTime " +
            "AND s.status != 'CANCELLED' " +
            "ORDER BY s.dateTime DESC " +
            "LIMIT 1")
    Optional<Session> findPreviousSession(
            @Param("hallId") Short hallId,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT s FROM Session s WHERE s.dateTime BETWEEN :start AND :end " +
            "ORDER BY s.dateTime, s.hall.hallName")
    List<Session> findSessionsForSchedule(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT s.film FROM Session s WHERE s.dateTime >= CURRENT_DATE")
    List<Film> findFilmsWithUpcomingSessions();
}