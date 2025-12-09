package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "genre")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private short genre_id;

    @Column(name = "genre_name", length = 50)
    private String genre_name;

    @OneToMany(mappedBy = "genre")
    private List<FilmGenre> film_genre_list = new ArrayList<>();
}
