package ru.yandex.practicum.commerce.store.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductState;
import ru.yandex.practicum.commerce.dto.store.QuantityState;

import java.util.UUID;

@Entity
@Table(name = "products")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "productId")
@Setter
@Getter
public class Product {
    @Id
    @UuidGenerator
    UUID productId;

    String productName;

    String description;

    String imageSrc;

    @Enumerated(EnumType.STRING)
    QuantityState quantityState;

    @Enumerated(EnumType.STRING)
    ProductState productState;

    @Enumerated(EnumType.STRING)
    ProductCategory productCategory;

    float price;
}
