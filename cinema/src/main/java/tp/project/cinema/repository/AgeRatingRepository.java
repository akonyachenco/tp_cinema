package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.AgeRating;

import java.util.Optional;

@Repository
public interface AgeRatingRepository extends JpaRepository<AgeRating, Short> {

    Optional<AgeRating> findByRatingValue(String ratingValue);
}