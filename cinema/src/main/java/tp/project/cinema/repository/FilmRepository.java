package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {

    Optional<Film> findByTitle(String title);

    List<Film> findByTitleContainingIgnoreCase(String title);

    List<Film> findByReleaseDateAfter(LocalDate date);

    List<Film> findByReleaseDateBefore(LocalDate date);

    List<Film> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT f FROM Film f WHERE f.duration <= :maxDuration")
    List<Film> findByDurationLessThanEqual(@Param("maxDuration") Short maxDuration);

    @Query("SELECT f FROM Film f WHERE f.duration >= :minDuration")
    List<Film> findByDurationGreaterThanEqual(@Param("minDuration") Short minDuration);

    @Query("SELECT f FROM Film f JOIN f.filmGenreList fg JOIN fg.genre g WHERE g.genreName = :genreName")
    List<Film> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT f FROM Film f WHERE f.director.directorId = :directorId")
    List<Film> findByDirectorId(@Param("directorId") Integer directorId);

    @Query("SELECT f FROM Film f WHERE f.country.countryId = :countryId")
    List<Film> findByCountryId(@Param("countryId") Short countryId);

    @Query("SELECT f FROM Film f WHERE f.ageRating.ratingValue = :ageRating")
    List<Film> findByAgeRating(@Param("ageRating") String ageRating);

    @Query("SELECT f FROM Film f WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Film> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT f FROM Film f JOIN f.sessionList s WHERE s.sessionId = :sessionId")
    Optional<Film> findBySessionId(@Param("sessionId") Integer sessionId);

    @Query("SELECT f FROM Film f WHERE f.releaseDate >= CURRENT_DATE ORDER BY f.releaseDate ASC")
    List<Film> findUpcomingFilms();

    @Query("SELECT f FROM Film f WHERE f.releaseDate <= CURRENT_DATE ORDER BY f.releaseDate DESC")
    List<Film> findReleasedFilms();
}