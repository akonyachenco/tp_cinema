package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticket_id;

    @CreationTimestamp
    @Column(name = "creation_date")
    private LocalDateTime creation_date;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "ticket_code", length = 20, unique = true)
    private String ticket_code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    public Long getTicketId() {
        return ticket_id;
    }

    public LocalDateTime getCreationDate() {
        return creation_date;
    }

    public String getTicketCode() {
        return ticket_code;
    }
}