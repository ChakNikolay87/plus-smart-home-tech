package ru.yandex.practicum.commerce.store.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.exception.ApiError;
import ru.yandex.practicum.commerce.exception.ProductNotFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFoundException(ProductNotFoundException ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ApiError> buildResponseEntity(Exception ex, HttpStatus status) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(
                ApiError.builder()
                        .status(status.toString())
                        .reason(ex.getClass().toString())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build(),
                status
        );
    }
}
