package tp.project.cinema.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long user_id;

    @CreationTimestamp
    @Column(name = "registration_date")
    private LocalDate registration_date;

    @Column(name = "surname", length = 40)
    private String surname;

    @Column(name = "name", length = 40)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birth_date;

    @Transient
    private Short age;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "role", length = 20)
    private String role;

    @OneToMany(mappedBy = "user")
    private List<Booking> booking_list = new ArrayList<>();

    public Short getAge() {
        if (birth_date == null) {
            return 0;
        }
        return (short) Period.between(birth_date, LocalDate.now()).getYears();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password_hash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getUserId() {
        return user_id;
    }

    public LocalDate getRegistrationDate() {
        return registration_date;
    }

    public LocalDate getBirthDate() {
        return birth_date;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birth_date = birthDate;
    }
}