package ru.yandex.practicum.commerce.cart.exception;

import feign.FeignException;
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

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ApiError> handleNotAuthorizedUserException(NotAuthorizedUserException ex) {
        return buildResponseEntity(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<ApiError> handleNoProductsInShoppingCartException(NoProductsInShoppingCartException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShoppingCartDeactivationException.class)
    public ResponseEntity<ApiError> handleShoppingCartDeactivationException(ShoppingCartDeactivationException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    public ResponseEntity<ApiError> handleProductInShoppingCartLowQuantityInWarehouse(ProductInShoppingCartLowQuantityInWarehouse ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ApiError> handleRemoteServiceException(RemoteServiceException ex) {
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiError> handleFeignException(FeignException ex) {
        if (ex.status() == HttpStatus.NOT_FOUND.value()) {
            return buildResponseEntity(
                    new ProductInShoppingCartLowQuantityInWarehouse("Shopping cart has not passed the stock check"),
                    HttpStatus.BAD_REQUEST
            );
        } else {
            return buildResponseEntity(
                    new RemoteServiceException("Error in the remote service 'warehouse'"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    private ResponseEntity<ApiError> buildResponseEntity(Exception ex, HttpStatus status) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);
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
