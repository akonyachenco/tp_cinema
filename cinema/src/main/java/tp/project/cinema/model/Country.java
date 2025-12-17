package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "country")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private short country_id;

    @Column(name = "country_name", length = 50)
    private String country_name;

    @OneToMany(mappedBy = "country")
    private List<Director> director_list = new ArrayList<>();

    @OneToMany(mappedBy = "country")
    private List<Film> film_list = new ArrayList<>();
}
