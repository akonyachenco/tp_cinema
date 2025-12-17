package tp.project.cinema.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserDto {
    private Long userId;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
    private Short age;
    private LocalDate registrationDate;
    private String role;
    private List<BookingDto> bookingList;
}