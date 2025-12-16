package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.FilmGenre;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilmGenreRepository extends JpaRepository<FilmGenre, Long> {

    List<FilmGenre> findByFilmFilmId(Long filmId);

    List<FilmGenre> findByGenreGenreId(Short genreId);

    @Query("SELECT fg FROM FilmGenre fg WHERE fg.film.filmId = :filmId AND fg.genre.genreId = :genreId")
    Optional<FilmGenre> findByFilmAndGenre(
            @Param("filmId") Long filmId,
            @Param("genreId") Short genreId);

    @Modifying
    @Query("DELETE FROM FilmGenre fg WHERE fg.film.filmId = :filmId")
    void deleteByFilmId(@Param("filmId") Long filmId);

    @Modifying
    @Query("DELETE FROM FilmGenre fg WHERE fg.genre.genreId = :genreId")
    void deleteByGenreId(@Param("genreId") Short genreId);

    @Modifying
    @Query("DELETE FROM FilmGenre fg WHERE fg.film.filmId = :filmId AND fg.genre.genreId = :genreId")
    void deleteByFilmAndGenre(
            @Param("filmId") Long filmId,
            @Param("genreId") Short genreId);

    boolean existsByFilmFilmIdAndGenreGenreId(Long filmId, Short genreId);
}