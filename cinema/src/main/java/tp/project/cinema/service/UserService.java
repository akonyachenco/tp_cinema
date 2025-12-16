package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.UserDto;
import tp.project.cinema.dto.UserRegisterDto;
import tp.project.cinema.dto.Mapping.UserMapping;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.exception.AlreadyExistsException;
import tp.project.cinema.model.User;
import tp.project.cinema.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapping userMapping;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapping::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));
        return userMapping.toDto(user);
    }

    public UserDto createUser(UserRegisterDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new AlreadyExistsException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = userMapping.toEntity(userDto);
        user.setPassword_hash(userDto.getPassword()); // Без шифрования
        user.setRegistration_date(LocalDate.now());
        user.setRole("USER");

        User savedUser = userRepository.save(user);
        return userMapping.toDto(savedUser);
    }

    public UserDto updateUser(Long id, UserRegisterDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));

        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new AlreadyExistsException("Email " + userDto.getEmail() + " уже используется");
        }

        existingUser.setName(userDto.getName());
        existingUser.setSurname(userDto.getSurname());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setBirth_date(userDto.getBirthDate());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword_hash(userDto.getPassword()); // Без шифрования
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapping.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + email + " не найден"));
        return userMapping.toDto(user);
    }

    public List<UserDto> searchUsers(String name, String surname, String email) {
        List<User> users;

        if (name != null && surname != null) {
            users = userRepository.findByNameOrSurnameContaining(name, surname);
        } else if (name != null) {
            users = userRepository.findByNameContainingIgnoreCase(name);
        } else if (surname != null) {
            users = userRepository.findBySurnameContainingIgnoreCase(surname);
        } else if (email != null) {
            users = userRepository.findByEmail(email)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(userMapping::toDto)
                .collect(Collectors.toList());
    }

    public UserDto changeUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));

        if (!List.of("USER", "ADMIN", "MODERATOR").contains(role.toUpperCase())) {
            throw new IllegalArgumentException("Недопустимая роль: " + role);
        }

        user.setRole(role.toUpperCase());
        User updatedUser = userRepository.save(user);
        return userMapping.toDto(updatedUser);
    }
}