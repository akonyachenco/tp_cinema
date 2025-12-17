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
    private short genreId;

    @Column(name = "genre_name", length = 50)
    private String genreName;

    @OneToMany(mappedBy = "genre")
    private List<FilmGenre> filmGenreList = new ArrayList<>();
}
