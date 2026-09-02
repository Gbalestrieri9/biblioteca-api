package com.postech.biblioteca.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Formato padronizado de erro retornado por todos os endpoints da API
 * (ver {@code GlobalExceptionHandler}).
 */
public record ErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<Map<String, String>> validationErrors
) {

    public static ErrorResponseDTO of(int status, String error, String message, String path) {
        return new ErrorResponseDTO(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponseDTO of(int status, String error, String message, String path,
                                       List<Map<String, String>> validationErrors) {
        return new ErrorResponseDTO(Instant.now(), status, error, message, path, validationErrors);
    }
}
