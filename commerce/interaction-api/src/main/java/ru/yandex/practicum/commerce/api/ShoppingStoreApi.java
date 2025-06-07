package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

public interface ShoppingStoreApi {

    /**
     * Получение списка товаров по типу в пагинированном виде
     *
     * @param category - категория товара
     * @param pageable - объект пагинации (номер страницы, кол-во страниц и параметры сортировки)
     * @return - список товаров
     */
    @GetMapping
    List<ProductDto> getProducts(@RequestParam @Valid ProductCategory category, Pageable pageable);

    /**
     * Создание нового товара в ассортименте
     *
     * @param productDto - Описательная часть вновь добавляемого товара в систему, например нового роутера и т.д.
     * @return - DTO продукта с ID
     */
    @PutMapping
    ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto);

    /**
     * Обновление товара в ассортименте, например уточнение описания, характеристик и т.д.
     *
     * @param productDto - Описательная часть изменяемого товара в системе
     * @return - DTO обновленного продукта
     */
    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    /**
     * Удаление товара из ассортимента магазина. Функция для менеджерского состава.
     *
     * @param productID - UUID продукта
     */
    @PostMapping("/removeProductFromStore")
    void removeProductFromStore(@RequestBody UUID productID);

    /**
     * Установка статуса по товару. API вызывается со стороны склада.
     *
     * @param productQuantityStateRequest - ID товара и новый статус
     */
    @PostMapping("/quantityState")
    void setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest productQuantityStateRequest);

    /**
     * Получение сведений по товару из БД.
     *
     * @param productId - UUID товара
     * @return - DTO товара
     */
    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}
