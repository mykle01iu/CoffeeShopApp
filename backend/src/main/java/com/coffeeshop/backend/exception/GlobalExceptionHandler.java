
package com.coffeeshop.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Xu ly loi nghiep vu: 400, 404, 409...
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        int status = ex.getStatusCode().value();

        String message = ex.getReason() != null
                ? ex.getReason()
                : "Yeu cau khong hop le";

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(buildError(
                        status,
                        HttpStatus.valueOf(status).getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }

    // Xu ly JSON sai dinh dang
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest().body(
                buildError(
                        400,
                        "Bad Request",
                        "Du lieu JSON khong hop le",
                        request.getRequestURI()
                )
        );
    }

    // Tao cau truc JSON loi thong nhat
    private Map<String, Object> buildError(
            int status,
            String error,
            String message,
            String path) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);
        response.put("path", path);

        return response;
    }
}
