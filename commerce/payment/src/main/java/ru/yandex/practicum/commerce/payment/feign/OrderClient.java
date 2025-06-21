package ru.yandex.practicum.commerce.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.OrderApi;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient extends OrderApi {
}
