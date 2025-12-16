package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Director;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Integer> {

    List<Director> findByNameContainingIgnoreCase(String name);

    List<Director> findBySurnameContainingIgnoreCase(String surname);

    @Query("SELECT d FROM Director d WHERE d.name LIKE %:name% OR d.surname LIKE %:surname%")
    List<Director> findByNameOrSurnameContaining(
            @Param("name") String name,
            @Param("surname") String surname);

    List<Director> findByCountryCountryId(Short countryId);

    List<Director> findByBirthDateAfter(LocalDate date);

    List<Director> findByBirthDateBefore(LocalDate date);

    @Query("SELECT d FROM Director d WHERE d.directorId IN " +
            "(SELECT f.director.directorId FROM Film f GROUP BY f.director.directorId " +
            "HAVING COUNT(f) >= :minFilms)")
    List<Director> findDirectorsWithMinFilms(@Param("minFilms") Long minFilms);

    @Query("SELECT d, COUNT(f) as filmCount FROM Director d LEFT JOIN d.filmList f " +
            "GROUP BY d.directorId ORDER BY filmCount DESC")
    List<Object[]> findAllDirectorsWithFilmCount();
}