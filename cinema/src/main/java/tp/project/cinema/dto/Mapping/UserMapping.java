package tp.project.cinema.dto.Mapping;

import org.mapstruct.*;
import tp.project.cinema.dto.UserDto;
import tp.project.cinema.dto.UserRegisterDto;
import tp.project.cinema.model.User;

@Mapper(componentModel = "spring", uses = {BookingMapping.class})
public interface UserMapping {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "bookingList", ignore = true)
    @Mapping(source = "birthDate", target = "birthDate")
    User toEntity(UserRegisterDto dto);

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "registrationDate", target = "registrationDate")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(expression = "java(entity.getAge())", target = "age")
    UserDto toDto(User entity);

    @AfterMapping
    default void afterEntityMapping(@MappingTarget User entity, UserRegisterDto dto) {
        if (entity.getRole() == null) {
            entity.setRole("USER");
        }
    }
}