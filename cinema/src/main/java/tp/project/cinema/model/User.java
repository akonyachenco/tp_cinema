package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long user_id;

    @CreationTimestamp
    @Column(name = "registration_date")
    private LocalDate registration_date;

    @Column(name = "surname", length = 40)
    private String surname;

    @Column(name = "name", length = 40)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birth_date;

    @Transient
    private short age;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "role", length = 20)
    private String role;

    @OneToMany(mappedBy = "user")
    private List<Booking> booking_list = new ArrayList<>();

    public short getAge() {
        return (short) Period.between(birth_date, LocalDate.now()).getYears();
    }

}
