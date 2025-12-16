package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Short> {

    Optional<Genre> findByGenreName(String genreName);

    List<Genre> findByGenreNameContainingIgnoreCase(String genreName);

    @Query("SELECT g FROM Genre g WHERE g.genreId IN " +
            "(SELECT fg.genre.genreId FROM FilmGenre fg WHERE fg.film.filmId = :filmId)")
    List<Genre> findByFilmId(@Param("filmId") Long filmId);

    @Query("SELECT g.genreName, COUNT(fg) as filmCount FROM Genre g LEFT JOIN g.filmGenreList fg " +
            "GROUP BY g.genreId ORDER BY filmCount DESC")
    List<Object[]> findAllGenresWithFilmCount();

    @Query("SELECT g FROM Genre g WHERE g.genreId IN " +
            "(SELECT fg.genre.genreId FROM FilmGenre fg GROUP BY fg.genre.genreId " +
            "HAVING COUNT(fg) > 0)")
    List<Genre> findGenresWithFilms();
}