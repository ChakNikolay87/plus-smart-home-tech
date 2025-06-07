package ru.yandex.practicum.commerce.warehouse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.WarehouseApi;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
    private final WarehouseService warehouseService;

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("Received request for new product in warehouse {}", request);
        warehouseService.newProductInWarehouse(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCart) {
        log.info("Received request for check product quantity enough for shopping cart {}", shoppingCart);
        return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCart);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("Received request for add product to warehouse {}", request);
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Received request for get warehouse address");
        return warehouseService.getWarehouseAddress();
    }
}
