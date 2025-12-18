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
    private short hallId;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "rows_count")
    private short rowsCount;

    @Column(name = "seats_per_row")
    private short seatsPerRow;

    @Column(name = "hall_name", length = 50)
    private String hallName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_type_id", nullable = false)
    private HallType hallType;

    @OneToMany(mappedBy = "hall")
    private List<Session> sessionList = new ArrayList<>();

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seatList = new ArrayList<>();
}
