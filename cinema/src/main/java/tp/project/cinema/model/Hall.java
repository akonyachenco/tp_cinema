package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "hall")
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hall_id")
    private short hall_id;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "base_price")
    private BigDecimal base_price;

    @Column(name = "rows_count")
    private short rows_count;

    @Column(name = "seats_per_row")
    private short seats_per_row;

    @Column(name = "hall_name", length = 50)
    private String hall_name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_type_id", nullable = false)
    private HallType hall_type;

    @OneToMany(mappedBy = "hall")
    private List<Session> session_list = new ArrayList<>();

    @OneToMany(mappedBy = "hall")
    private List<Seat> seat_list = new ArrayList<>();
}
