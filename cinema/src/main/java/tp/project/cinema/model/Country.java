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
    private short countryId;

    @Column(name = "country_name", length = 50)
    private String countryName;

    @OneToMany(mappedBy = "country")
    private List<Director> directorList = new ArrayList<>();

    @OneToMany(mappedBy = "country")
    private List<Film> filmList = new ArrayList<>();
}
