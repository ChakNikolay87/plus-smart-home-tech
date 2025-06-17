package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Размеры товара
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DimensionDto {
    @NotBlank
    @Min(value = 1, message = "Width must be greater than 0")
    Double width; // ширина

    @NotBlank
    @Min(value = 1, message = "Height must be greater than 0")
    Double height; // высота

    @NotBlank
    @Min(value = 1, message = "Depth must be greater than 0")
    Double depth; // глубина
}
