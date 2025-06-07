package ru.yandex.practicum.commerce.cart.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "shoppingCartId")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID shoppingCartId;

    String username;

    boolean isActive = true; // статус корзины товаров (false - не активна, true - активна)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cart_products",
            joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    Map<UUID, Long> products;
}
