package com.example.identity_service.error;

import com.example.identity_service.error.exception.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    public ResponseEntity<Object> handleBadRequestException(BadRequestException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("path", "/api");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Object> handleConflictException(ConflictException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("path", "/api");

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
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
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException e) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("path", "/api");

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
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
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("path", "/api");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
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
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("path", "/api");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
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
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}