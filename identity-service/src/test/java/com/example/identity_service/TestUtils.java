package com.example.identity_service;

import com.example.identity_service.error.ApiError;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtils {

    public static void assertApiError(ApiError error, int expectedStatus, String expectedMessage, String expectedPath) {
        assertNotNull(error);
        assertEquals(expectedStatus, error.status());
        assertEquals(expectedMessage, error.message());
        assertEquals(expectedPath, error.path());
        assertNotNull(error.timestamp());
    }

    public static void assertBadRequest(ApiError error, String message, String path) {
        assertApiError(error, HttpStatus.BAD_REQUEST.value(), message, path);
    }

    public static void assertUnauthorized(ApiError error, String message, String path) {
        assertApiError(error, HttpStatus.UNAUTHORIZED.value(), message, path);
    }

    public static void assertNotFound(ApiError error, String message, String path) {
        assertApiError(error, HttpStatus.NOT_FOUND.value(), message, path);
    }

    public static void assertConflict(ApiError error, String message, String path) {
        assertApiError(error, HttpStatus.CONFLICT.value(), message, path);
    }

}
