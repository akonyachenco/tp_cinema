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
    private short hallTypeId;

    @Column(name = "type_name", length = 50)
    private String typeName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "hallType")
    private List<Hall> hallList = new ArrayList<>();
}
