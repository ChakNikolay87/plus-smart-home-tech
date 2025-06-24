package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.dto.order.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "orderId")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID orderId; // идентификатор заказа

    UUID shoppingCartId; // идентификатор корзины

    String username; // имя пользователя

    UUID paymentId; // идентификатор оплаты

    UUID deliveryId; // идентификатор доставки

    OrderState state; // статус заказа

    double deliveryWeight; // общий вес доставки

    double deliveryVolume; // общий объем доставки

    boolean fragile; // признак хрупкости заказа

    BigDecimal totalPrice; // общая стоимость заказа

    BigDecimal deliveryPrice; // стоимость доставки

    BigDecimal productPrice; // стоимость товаров в заказе

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    Map<UUID, Long> products; // ID товара и его количество
}
