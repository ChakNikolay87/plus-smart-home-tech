package ru.yandex.practicum.commerce.store.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.ShoppingStoreApi;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.store.service.StoreService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class StoreController implements ShoppingStoreApi {
    private final StoreService storeService;

    @Override
    public List<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("Received request to getProducts for category: {}", category);
        return storeService.getProducts(category, pageable);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        log.info("Received request to create new product: {}", productDto);
        return storeService.createNewProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Received request to update product: {}", productDto);
        return storeService.updateProduct(productDto);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void removeProductFromStore(UUID productID) {
        log.info("Received request to remove product from store: {}", productID);
        storeService.removeProductFromStore(productID);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void setProductQuantityState(SetProductQuantityStateRequest productQuantityStateRequest) {
        log.info("Received request to set product quantity state: {}", productQuantityStateRequest);
        storeService.setProductQuantityState(productQuantityStateRequest);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        log.info("Received request to get product by id: {}", productId);
        return storeService.getProduct(productId);
    }
}
