package ru.yandex.practicum.commerce.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.ProductState;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.model.Product;
import ru.yandex.practicum.commerce.store.repository.StoreRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("Getting products for category: {}", category);
        List<ProductDto> productDtos = storeRepository.findAllByProductCategory(category, pageable).stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();
        log.info("Found {} products", productDtos.size());
        return productDtos;
    }

    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        log.info("Creating new product: {}", productDto);
        Product product = modelMapper.map(productDto, Product.class);
        ProductDto newProductDto = modelMapper.map(storeRepository.save(product), ProductDto.class);
        log.info("New product created: {}", newProductDto);
        return newProductDto;
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Updating product: {}", productDto);
        findProduct(productDto.getProductId());
        Product product = modelMapper.map(productDto, Product.class);
        ProductDto updatedProductDto = modelMapper.map(storeRepository.save(product), ProductDto.class);
        log.info("Updated product: {}", updatedProductDto);
        return updatedProductDto;
    }

    @Transactional
    public void removeProductFromStore(UUID productID) {
        log.info("Removing product from store: {}", productID);
        Product product = findProduct(productID);
        product.setProductState(ProductState.DEACTIVATE);
        storeRepository.saveAndFlush(product);
        log.info("Removed product from store: {}", productID);
    }

    @Transactional
    public void setProductQuantityState(SetProductQuantityStateRequest productQuantityStateRequest) {
        log.info("Setting product quantity state: {}", productQuantityStateRequest);
        Product product = findProduct(productQuantityStateRequest.getProductId());
        product.setQuantityState(productQuantityStateRequest.getQuantityState());
        storeRepository.save(product);
        log.info("Set product quantity state: {}", productQuantityStateRequest);
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        log.info("Getting product: {}", productId);
        Product product = findProduct(productId);
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        log.info("Found product: {}", productDto);
        return productDto;
    }

    private Product findProduct(UUID productId) {
        return storeRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID=" + productId + " not found"));
    }
}
