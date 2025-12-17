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
    private short ratingId;

    @Column(name = "rating_value", length = 10)
    private String ratingValue;

    @OneToMany(mappedBy = "ageRating")
    private List<Film> filmList = new ArrayList<Film>();
}
