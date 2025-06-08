package ru.yandex.practicum.commerce.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Товар, продаваемый в интернет-магазине
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDto {
    UUID productId; // идентификатор товара в БД

    @NotBlank
    String productName; // наименование товара

    @NotBlank
    String description; // описание товара

    String imageSrc; // ссылка на картинку во внешнем хранилище или SVG

    @NotNull
    QuantityState quantityState; // статус, перечисляющий состояние остатка как свойства товара

    @NotNull
    ProductState productState; // статус товара

    ProductCategory productCategory; // категория товара

    @NotNull
    @Min(value = 1, message = "Price should be less than 1")
    Float price;
}
