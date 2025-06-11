package ru.yandex.practicum.commerce.dto.store;

/**
 * Статус, перечисляющий состояние остатка как свойства товара
 */
public enum QuantityState {
    ENDED,
    FEW,
    ENOUGH,
    MANY;


    public static QuantityState fromQuantity(long quantity) {
        if (quantity <= 0) {
            return ENDED;
        } else if (quantity < 10) {
            return FEW;
        } else if (quantity <= 100) {
            return ENOUGH;
        } else {
            return MANY;
        }
    }
}
