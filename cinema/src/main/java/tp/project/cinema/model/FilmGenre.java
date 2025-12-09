package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "film_genre")
public class FilmGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "film_genre_id")
    private long film_genre_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;
}
