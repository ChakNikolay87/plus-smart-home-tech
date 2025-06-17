package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Dimension {
    double width; // ширина
    double height; // высота
    double depth; // глубина
}
