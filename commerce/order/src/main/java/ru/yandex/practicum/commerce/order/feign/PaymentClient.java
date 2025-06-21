package ru.yandex.practicum.commerce.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.PaymentApi;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient extends PaymentApi {
}
