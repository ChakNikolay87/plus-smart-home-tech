package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseApi {
    /**
     * Добавление нового товара на склад
     *
     * @param request - DTO товара NewProductInWarehouseRequest
     */
    @PutMapping
    void newProductInWarehouse(@RequestBody @Valid NewProductInWarehouseRequest request);

    /**
     * Предварительная проверка, что количество товаров на складе достаточно для данной корзины продуктов
     *
     * @param shoppingCart - корзина товаров
     * @return - общие сведения по бронированию
     */
    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCart);

    /**
     * Принятие товара на склад
     *
     * @param request - запрос на добавление определенного количества определенного товара
     */
    @PostMapping("/add")
    void addProductToWarehouse(@RequestBody @Valid AddProductToWarehouseRequest request);

    /**
     * Предоставление адреса для расчета доставки
     *
     * @return - DTO адреса
     */
    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    /**
     * Передача товаров в доставку
     *
     * @param request - запрос на передачу в доставку
     */
    @PostMapping("/shipped")
    void shippedToDelivery(@RequestBody @Valid ShippedToDeliveryRequest request);

    /**
     * Возврат товаров на склад
     *
     * @param products - отображение идентификатора товара на отобранное количество
     */
    @PostMapping("/return")
    void acceptReturn(@RequestBody Map<UUID, Long> products);

    /**
     * Сборка товаров к заказу для подготовки к отправке
     *
     * @param request - корзина товаров
     * @return - общие сведения по бронированию
     */
    @PostMapping("/assembly")
    BookedProductsDto assemblyProductsForOrder(@RequestBody @Valid AssemblyProductsForOrderRequest request);
}
