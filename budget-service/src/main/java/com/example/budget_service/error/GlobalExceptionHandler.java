package com.example.budget_service.error;

import com.example.budget_service.error.exception.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = ApiError.class,
                    example = "{\n" +
                            "  \"timestamp\": \"2025-03-12T19:46:16.392\",\n" +
                            "  \"status\": 400,\n" +
                            "  \"message\": \"Invalid request data\",\n" +
                            "  \"path\": \"/api/auth/register\"\n" +
                            "}"
            ))
    )
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = ApiError.class,
                    example = "{\n" +
                            "  \"timestamp\": \"2025-03-12T19:46:16.392\",\n" +
                            "  \"status\": 409,\n" +
                            "  \"message\": \"User already exists\",\n" +
                            "  \"path\": \"/api/auth/register\"\n" +
                            "}"
            ))
    )
    public ResponseEntity<ApiError> handleConflictException(ConflictException e, HttpServletRequest request) {

        ApiError error = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = ApiError.class,
                    example = "{\n" +
                            "  \"timestamp\": \"2025-03-12T19:46:16.392\",\n" +
                            "  \"status\": 403,\n" +
                            "  \"message\": \"Access denied\",\n" +
                            "  \"path\": \"/api/admin\"\n" +
                            "}"
            ))
    )
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {

        ApiError error = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = ApiError.class,
                    example = "{\n" +
                            "  \"timestamp\": \"2025-03-12T19:46:16.392\",\n" +
                            "  \"status\": 404,\n" +
                            "  \"message\": \"User not found\",\n" +
                            "  \"path\": \"/api/users/1\"\n" +
                            "}"
            ))
    )
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = ApiError.class,
                    example = "{\n" +
                            "  \"timestamp\": \"2025-03-12T19:46:16.392\",\n" +
                            "  \"status\": 401,\n" +
                            "  \"message\": \"Invalid or expired token\",\n" +
                            "  \"path\": \"/api/auth/refresh\"\n" +
                            "}"
            ))
    )
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = "{\n" +
                                    "  \"timestamp\": \"2025-03-12T19:46:16.392\",\n" +
                                    "  \"status\": 400,\n" +
                                    "  \"errors\": {\n" +
                                    "    \"lastName\": \"Last name is required\",\n" +
                                    "    \"email\": \"Invalid email format\"\n" +
                                    "  }\n" +
                                    "}"
                    )
            )
    )
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        ApiError error = new ApiError(
                request.getRequestURI(),
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.badRequest().body(error);
    }

}