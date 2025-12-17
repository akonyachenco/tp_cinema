package tp.project.cinema.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tp.project.cinema.dto.*;
import tp.project.cinema.dto.Mapping.UserMapping;
import tp.project.cinema.model.User;
import tp.project.cinema.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapping userMapping;

    public UserDto register(UserRegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }

        User user = userMapping.toEntity(registerDto);
        user.setPassword_hash(passwordEncoder.encode(registerDto.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapping.toDto(savedUser);
    }

    public AuthResponseDto login(UserLoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String token = jwtTokenUtil.generateToken(user);
            UserDto userDto = userMapping.toDto(user);

            return AuthResponseDto.builder()
                    .token(token)
                    .user(userDto)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Неверный email или пароль");
        }
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            User user = (User) authentication.getPrincipal();
            return userMapping.toDto(user);
        } catch (ClassCastException e) {
            return null;
        }
    }
}