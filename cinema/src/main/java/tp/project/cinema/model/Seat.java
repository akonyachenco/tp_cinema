package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer seat_id;

    @Column(name = "row_number")
    private Short row_number;

    @Column(name = "seat_number")
    private Short seat_number;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seat_type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @OneToMany(mappedBy = "seat")
    private List<Ticket> ticket_list = new ArrayList<>();

    public Short getRowNumber() {
        return row_number;
    }

    public Short getSeatNumber() {
        return seat_number;
    }
}