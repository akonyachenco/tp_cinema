package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.UserDto;
import tp.project.cinema.dto.UserRegisterDto;
import tp.project.cinema.model.User;

@Mapper(componentModel = "spring", uses = {BookingMapping.class})
public interface UserMapping {

    @Mapping(target = "user_id", ignore = true)
    @Mapping(target = "password_hash", ignore = true)
    @Mapping(target = "registration_date", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "booking_list", ignore = true)
    @Mapping(source = "birthDate", target = "birth_date")
    User toEntity(UserRegisterDto dto);

    @Mapping(source = "user_id", target = "userId")
    @Mapping(source = "registration_date", target = "registrationDate")
    @Mapping(source = "birth_date", target = "birthDate")
    @Mapping(expression = "java(entity.getAge())", target = "age")
    UserDto toDto(User entity);

    @AfterMapping
    default void afterEntityMapping(@MappingTarget User entity, UserRegisterDto dto) {
        if (entity.getRole() == null) {
            entity.setRole("USER");
        }
    }
}