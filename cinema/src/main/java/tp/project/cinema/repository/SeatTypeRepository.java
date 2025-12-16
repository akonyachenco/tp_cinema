package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.SeatType;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, Short> {

    Optional<SeatType> findByTypeName(String typeName);

    Optional<SeatType> findByPriceMultiplier(BigDecimal priceMultiplier);
}