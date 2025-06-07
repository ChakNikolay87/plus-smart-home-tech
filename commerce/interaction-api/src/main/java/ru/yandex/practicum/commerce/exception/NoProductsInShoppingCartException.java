package ru.yandex.practicum.commerce.exception;

public class NoProductsInShoppingCartException extends RuntimeException {
    public NoProductsInShoppingCartException(final String message) {
        super(message);
    }
}
