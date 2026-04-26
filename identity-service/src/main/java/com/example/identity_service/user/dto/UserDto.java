package com.example.identity_service.user.dto;

import com.example.identity_service.authentication.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String id;

    private String firstName;

    private String lastName;

    private String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Role role;
}
