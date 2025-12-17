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
    private Short bookingStatusId;

    @Column(name = "status_name", length = 40, unique = true)
    private String statusName;

    @OneToMany(mappedBy = "bookingStatus")
    private List<Booking> bookingList = new ArrayList<>();

}