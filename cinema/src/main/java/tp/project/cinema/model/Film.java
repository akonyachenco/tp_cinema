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
    private Long film_id;

    @Getter
    @Column(name = "duration")
    private short duration;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "release_date")
    private LocalDate release_date;

    @Column(name = "poster_url")
    private String poster_url;

    @Column(name = "trailer_url")
    private String trailer_url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private Director director;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rating_id", nullable = false)
    private AgeRating age_rating;

    @OneToMany(mappedBy = "film")
    private List<FilmGenre> film_genre_list = new ArrayList<>();

    @OneToMany(mappedBy = "film")
    private List<Session> session_list = new ArrayList<>();

    public void setDuration(short duration) {
        this.duration = duration;
    }

    public void setDuration(Integer duration) {
        if (duration != null) {
            this.duration = duration.shortValue();
        }
    }
}