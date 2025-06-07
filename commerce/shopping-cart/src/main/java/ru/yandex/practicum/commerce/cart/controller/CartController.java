package ru.yandex.practicum.commerce.cart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.ShoppingCartApi;
import ru.yandex.practicum.commerce.cart.service.CartService;
import ru.yandex.practicum.commerce.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController implements ShoppingCartApi {
    private final CartService cartService;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        log.info("Received request to get shopping cart for user: {}", username);
        return cartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        log.info("Received request to add product to shopping cart: {}", username);
        return cartService.addProductToShoppingCart(username, products);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void deactivateCurrentShoppingCart(String username) {
        log.info("Received request to deactivate shopping cart: {}", username);
        cartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, Set<UUID> products) {
        log.info("Received request to remove product from shopping cart: {}", username);
        return cartService.removeFromShoppingCart(username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("Received request to change product quantity for user: {}", username);
        return cartService.changeProductQuantity(username, request);
    }
}
