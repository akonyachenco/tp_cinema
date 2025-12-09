package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "director")
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "director_id")
    private int director_id;

    @Column(name = "name", length = 40)
    private String name;

    @Column(name = "surname", length = 40)
    private String surname;

    @Column(name = "birth_date")
    private Date birth_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @OneToMany(mappedBy = "director")
    private List<Film> film_list = new ArrayList<>();
}
