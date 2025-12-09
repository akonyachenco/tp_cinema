package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "seat_type")
public class SeatType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_type_id")
    private short seat_type_id;

    @Column(name = "price_multiplier")
    private BigDecimal price_multiplier;

    @Column(name = "type_name", length = 50)
    private String type_name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "seat_type")
    private List<Seat> seat_list = new ArrayList<Seat>();

}
