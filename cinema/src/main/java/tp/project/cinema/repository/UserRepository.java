package tp.project.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.project.cinema.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findBySurnameContainingIgnoreCase(String surname);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% OR u.surname LIKE %:surname%")
    List<User> findByNameOrSurnameContaining(
            @Param("name") String name,
            @Param("surname") String surname);

    List<User> findByRole(String role);

    List<User> findByRegistrationDateAfter(LocalDate date);

    List<User> findByBirthDateBefore(LocalDate date);

    @Query("SELECT u FROM User u WHERE u.registrationDate BETWEEN :startDate AND :endDate")
    List<User> findUsersRegisteredBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);

    @Query("SELECT u FROM User u JOIN u.bookingList b WHERE b.bookingId = :bookingId")
    Optional<User> findByBookingId(@Param("bookingId") Long bookingId);
}