package com.example.budget_service.error;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(

        String path,
        String message,
        int status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp,
        Map<String, String> errors

) {}