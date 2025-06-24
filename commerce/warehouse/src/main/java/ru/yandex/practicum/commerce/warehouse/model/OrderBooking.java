package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class OrderBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID orderBookingId;

    UUID orderId;

    UUID deliveryId;

    boolean fragile;

    double deliveryVolume;

    double deliveryWeight;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "order_booking_products",
            joinColumns = @JoinColumn(name = "order_booking_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    Map<UUID, Long> products;
}