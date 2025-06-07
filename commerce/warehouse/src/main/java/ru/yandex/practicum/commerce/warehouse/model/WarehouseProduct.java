package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseProduct {
    @Id
    UUID productId;

    boolean fragile;

    @Embedded
    Dimension dimension;

    double weight;

    long quantity;
}
