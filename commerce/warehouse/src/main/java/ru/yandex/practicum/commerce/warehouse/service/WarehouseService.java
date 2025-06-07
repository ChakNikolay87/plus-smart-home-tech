package ru.yandex.practicum.commerce.warehouse.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.store.QuantityState;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.exception.*;
import ru.yandex.practicum.commerce.warehouse.feign.ShoppingStoreClient;
import ru.yandex.practicum.commerce.warehouse.model.Dimension;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];
    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;
    private final ShoppingStoreClient shoppingStoreClient;

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("Processing the addition new product in warehouse: {}", request);
        if (warehouseRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Product with ID = " + request.getProductId() + " already exists");
        }
        WarehouseProduct product = modelMapper.map(request, WarehouseProduct.class);
        warehouseRepository.save(product);
        log.info("New product added to warehouse: {}", product);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCart) {
        log.info("Checking product quantity enough for shopping cart: {}", shoppingCart);
        // получим мапу ID товаров и требуемое количество из корзины
        Map<UUID, Long> cartProducts = shoppingCart.getProducts();
        // получим набор ID всех товаров из корзины
        Set<UUID> productIds = cartProducts.keySet();
        // получим из БД мапу ID товаров и самих товаров по набору из корзины
        Map<UUID, WarehouseProduct> warehouseProducts = warehouseRepository.findAllByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        // обработаем каждый товар из корзины и проверим количество на складе
        // и сразу определим общий вес и объем доставки, и есть ли хрупкие предметы
        double totalWeight = 0;
        double totalVolume = 0;
        boolean isFragile = false;

        for (Map.Entry<UUID, Long> entry : cartProducts.entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = cartProducts.get(productId);
            if (!warehouseProducts.containsKey(productId)) {
                throw new NoSpecifiedProductInWarehouseException("Product with ID = " + productId + " does not exist in warehouse");
            }
            WarehouseProduct warehouseProduct = warehouseProducts.get(productId);
            if (requiredQuantity > warehouseProduct.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Product with ID = " + productId + " is low quantity of " +
                        requiredQuantity + ". In stock: " + warehouseProduct.getQuantity());
            }
            totalWeight += requiredQuantity * warehouseProduct.getWeight();
            totalVolume += requiredQuantity * getVolume(warehouseProduct.getDimension());
            if (!isFragile && warehouseProduct.isFragile()) isFragile = true;
        }

        log.info("Product quantity enough for shopping cart is checked for: {}", shoppingCart);
        return BookedProductsDto.builder()
                .fragile(isFragile)
                .deliveryVolume(totalVolume)
                .deliveryWeight(totalWeight)
                .build();
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("Adding product to warehouse: {}", request);
        WarehouseProduct warehouseProduct = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product with ID = " + request.getProductId() + " not found"));
        warehouseProduct.setQuantity(warehouseProduct.getQuantity() + request.getQuantity());
        warehouseRepository.save(warehouseProduct);

        // изменяем признак количества товара в Store
        try {
            log.info("Setting product quantity state for product in shopping store: {}", warehouseProduct);
            shoppingStoreClient.setProductQuantityState(SetProductQuantityStateRequest.builder()
                    .productId(request.getProductId())
                    .quantityState(getQuantityState(warehouseProduct.getQuantity()))
                    .build());
            log.info("Quantity state for product in shopping store: {} is set", warehouseProduct);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ProductNotFoundException("Error during setting product quantity state for product in shopping store");
            } else {
                throw new RemoteServiceException("Error in the remote service 'shopping-store'");
            }
        }
    }

    public AddressDto getWarehouseAddress() {
        log.info("Getting address from warehouse");
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    private double getVolume(Dimension dimension) {
        return dimension.getDepth() * dimension.getHeight() * dimension.getWidth();
    }

    private QuantityState getQuantityState(long quantity) {
        if (quantity <= 0) {
            return QuantityState.ENDED;
        } else if (quantity < 10) {
            return QuantityState.FEW;
        } else if (quantity <= 100) {
            return QuantityState.ENOUGH;
        } else {
            return QuantityState.MANY;
        }
    }
}
