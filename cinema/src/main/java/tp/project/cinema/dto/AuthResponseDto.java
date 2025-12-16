package tp.project.cinema.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String token;
    private UserDto user;
    private String tokenType = "Bearer";
}
