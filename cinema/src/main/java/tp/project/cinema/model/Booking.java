package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private long booking_id;

    @Column(name = "booking_time")
    private LocalDateTime booking_time;

    @Column(name = "total_cost")
    private BigDecimal total_cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_status_id", nullable = false)
    private BookingStatus booking_status;

    @OneToMany(mappedBy = "booking")
    private List<Ticket> ticket_list = new ArrayList<>();
}
