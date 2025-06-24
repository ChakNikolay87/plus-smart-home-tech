package ru.yandex.practicum.commerce.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.WarehouseApi;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient extends WarehouseApi {
}
