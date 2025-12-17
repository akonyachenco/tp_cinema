package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Country;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Short> {

    Optional<Country> findByCountryName(String countryName);

    List<Country> findByCountryNameContainingIgnoreCase(String countryName);

    @Query("SELECT c FROM Country c WHERE c.countryId IN " +
            "(SELECT f.country.countryId FROM Film f GROUP BY f.country.countryId " +
            "HAVING COUNT(f) > 0)")
    List<Country> findCountriesWithFilms();

    @Query("SELECT c.countryName, COUNT(f) as filmCount FROM Country c LEFT JOIN c.filmList f " +
            "GROUP BY c.countryId ORDER BY filmCount DESC")
    List<Object[]> findAllCountriesWithFilmCount();

    //Country findCountryByFilmId(long filmId);
}