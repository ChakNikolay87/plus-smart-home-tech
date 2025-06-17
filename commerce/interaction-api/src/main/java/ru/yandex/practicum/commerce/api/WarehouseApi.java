package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;

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
}
