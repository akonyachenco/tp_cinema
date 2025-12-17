package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "film")
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "film_id")
    private Long filmId;

    @Getter
    @Column(name = "duration")
    private short duration;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private Director director;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rating_id", nullable = false)
    private AgeRating ageRating;

    @OneToMany(mappedBy = "film")
    private List<FilmGenre> filmGenreList = new ArrayList<>();

    @OneToMany(mappedBy = "film")
    private List<Session> sessionList = new ArrayList<>();

    public void setDuration(short duration) {
        this.duration = duration;
    }

    public void setDuration(Integer duration) {
        if (duration != null) {
            this.duration = duration.shortValue();
        }
    }
}