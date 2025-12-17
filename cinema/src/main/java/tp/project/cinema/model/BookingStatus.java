package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "booking_status")
public class BookingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_status_id")
    private Short booking_status_id;

    @Column(name = "status_name", length = 40, unique = true)
    private String status_name;

    @OneToMany(mappedBy = "booking_status")
    private List<Booking> booking_list = new ArrayList<>();

    public Short getBookingStatusId() {
        return booking_status_id;
    }

    public String getStatusName() {
        return status_name;
    }
}