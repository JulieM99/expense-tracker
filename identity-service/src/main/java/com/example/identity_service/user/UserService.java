package com.example.identity_service.user;

import com.example.identity_service.authentication.TokenRepository;
import com.example.identity_service.error.exception.UnauthorizedException;
import com.example.identity_service.user.dto.ChangePasswordRequest;
import com.example.identity_service.user.dto.UpdateUserRequest;
import com.example.identity_service.user.dto.UserDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto getCurrentUser(Authentication authentication) {

        log.info("USER SERVICE getCurrentUser user emial={}", authentication.getName());

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toDto(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {

        log.info("USER SERVICE changePassword userId={}", user.getId());

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserDto updateUser(User user, @Valid UpdateUserRequest request) {

        log.info("USER SERVICE updateUser userId={}", user.getId());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }
}
