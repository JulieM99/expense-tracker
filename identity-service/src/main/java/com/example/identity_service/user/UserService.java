package com.example.identity_service.user;

import com.example.identity_service.common.event.UserDeleteEvent;
import com.example.identity_service.error.exception.NotFoundException;
import com.example.identity_service.error.exception.UnauthorizedException;
import com.example.identity_service.user.dto.ChangePasswordRequest;
import com.example.identity_service.user.dto.UpdateUserRequest;
import com.example.identity_service.user.dto.UserDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public UserDto getCurrentUser(User user) {

        log.info("USER SERVICE getCurrentUser user emial={}", user.getEmail());

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

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User saved = userRepository.save(user);

        log.info("USER SERVICE successfully updatedUser - userId={}", user.getId());

        return userMapper.toDto(saved);
    }

    @Transactional
    public void deleteUser(User user) {

        log.info("USER SERVICE deleteUser userId={}", user.getId());

        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        eventPublisher.publishEvent(
                new UserDeleteEvent(existing.getEmail(), existing.getFirstName())
        );

        userRepository.delete(existing);
    }
}
