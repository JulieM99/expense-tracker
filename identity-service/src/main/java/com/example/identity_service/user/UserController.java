package com.example.identity_service.user;


import com.example.identity_service.error.ApiError;
import com.example.identity_service.user.dto.ChangePasswordRequest;
import com.example.identity_service.user.dto.UpdateUserRequest;
import com.example.identity_service.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    public static final String Users_PATH = "/api/users";
    private final UserService userService;

    @Operation(
            summary = "Get current authenticated user",
            description = "Returns information about the currently authenticated user based on the provided JWT access token.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully retrieved",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(userService.getCurrentUser(user));
    }

    @Operation(
            summary = "Change user password",
            description = "Allows authenticated user to change their password by providing old and new password. Requires valid JWT access token.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Password successfully changed"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired token / wrong old password",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User user = (User) authentication.getPrincipal();

        userService.changePassword(user, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update current user profile",
            description = "Allows authenticated user to update their first and last name.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateUserData(Authentication authentication, @Valid @RequestBody UpdateUserRequest request) {
        User user = (User) authentication.getPrincipal();

        UserDto updated = userService.updateUser(user, request);

        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete user",
            description = "Allows authenticated user to delete their account.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "User deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userService.deleteUser(user);

        return ResponseEntity.noContent().build();
    }

}
