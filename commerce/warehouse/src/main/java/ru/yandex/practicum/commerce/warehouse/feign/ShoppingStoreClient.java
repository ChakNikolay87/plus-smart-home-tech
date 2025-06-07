package ru.yandex.practicum.commerce.warehouse.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.ShoppingStoreApi;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreApi {
}
