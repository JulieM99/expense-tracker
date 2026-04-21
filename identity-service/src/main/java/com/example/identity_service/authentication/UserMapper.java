package com.example.identity_service.authentication;
import com.example.identity_service.authentication.dto.RegisterRequest;
import com.example.identity_service.authentication.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .passwordHash(request.password())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
    }

    public abstract UserDto toDto(User user);
}
