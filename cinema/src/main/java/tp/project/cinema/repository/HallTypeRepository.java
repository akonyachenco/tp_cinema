package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.HallType;

import java.util.Optional;

@Repository
public interface HallTypeRepository extends JpaRepository<HallType, Short> {
    Optional<HallType> findByTypeName(String typeName);
}