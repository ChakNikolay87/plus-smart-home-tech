package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ShoppingCartApi {
    /**
     * Получение актуальной корзины для авторизованного пользователя
     *
     * @param username - имя пользователя
     * @return - DTO корзины пользователя
     */
    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String username);

    /**
     * Добавление товара в корзину
     *
     * @param username - имя пользователя
     * @param products - отображение идентификатора товара на отобранное количество
     * @return - DTO корзины пользователя
     */
    @PutMapping
    ShoppingCartDto addProductToShoppingCart(@RequestParam @NotBlank String username, @RequestBody Map<UUID, Long> products);

    /**
     * Деактивация корзины товаров для пользователя
     *
     * @param username - имя пользователя
     */
    @DeleteMapping
    void deactivateCurrentShoppingCart(@RequestParam @NotBlank String username);

    /**
     * Изменение состава товаров в корзине, т.е. удаление других товаров
     *
     * @param username - имя пользователя
     * @param products - отображение идентификатора товара на отобранное количество
     * @return - DTO корзины пользователя
     */
    @PostMapping("/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam @NotBlank String username, @RequestBody Set<UUID> products);

    /**
     * Изменение количества товара в корзине
     *
     * @param username - имя пользователя
     * @param request  - запрос на изменение количества товара
     * @return - DTO корзины пользователя
     */
    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam @NotBlank String username, @RequestBody @Valid ChangeProductQuantityRequest request);
}
