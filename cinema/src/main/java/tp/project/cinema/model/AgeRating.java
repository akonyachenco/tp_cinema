package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "age_rating")
public class AgeRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private short rating_id;

    @Column(name = "rating_value", length = 10)
    private String rating_value;

    @OneToMany(mappedBy = "age_rating")
    private List<Film> film_list = new ArrayList<Film>();
}
