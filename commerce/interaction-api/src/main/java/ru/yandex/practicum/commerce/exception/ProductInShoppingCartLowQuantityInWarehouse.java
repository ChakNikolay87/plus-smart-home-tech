package ru.yandex.practicum.commerce.exception;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {
    public ProductInShoppingCartLowQuantityInWarehouse(final String message) {
        super(message);
    }
}
