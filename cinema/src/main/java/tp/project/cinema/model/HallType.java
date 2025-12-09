package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "hall_type")
public class HallType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hall_type_id")
    private short hall_type_id;

    @Column(name = "type_name", length = 50)
    private String type_name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "hall_type")
    private List<Hall> hall_list = new ArrayList<>();
}
