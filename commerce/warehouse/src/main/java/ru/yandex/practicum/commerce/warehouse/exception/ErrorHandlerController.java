package ru.yandex.practicum.commerce.warehouse.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.exception.*;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    public ResponseEntity<ApiError> handleProductInShoppingCartLowQuantityInWarehouse(ProductInShoppingCartLowQuantityInWarehouse ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    public ResponseEntity<ApiError> handleNoSpecifiedProductInWarehouseException(NoSpecifiedProductInWarehouseException ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<ApiError> handleSpecifiedProductAlreadyInWarehouseException(SpecifiedProductAlreadyInWarehouseException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFoundException(ProductNotFoundException ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ApiError> handleRemoteServiceException(RemoteServiceException ex) {
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
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
