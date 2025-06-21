package ru.yandex.practicum.commerce.delivery.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.delivery.feign.OrderClient;
import ru.yandex.practicum.commerce.delivery.feign.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.model.Address;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.exception.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.exception.RemoteServiceException;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {
    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0); // базовая ставка доставки
    private static final BigDecimal ADDRESS1_RATE = BigDecimal.valueOf(1.0); // коэффициент умножения для доставки по адресу 1
    private static final BigDecimal ADDRESS2_RATE = BigDecimal.valueOf(2.0); // коэффициент умножения для доставки по адресу 2
    private static final BigDecimal FRAGILE_RATE = BigDecimal.valueOf(0.2); // коэффициент умножения для хрупкого товара
    private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.3); // коэффициент умножения для веса
    private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(0.2); // коэффициент умножения для объема
    private static final BigDecimal STREET_RATE = BigDecimal.valueOf(0.2); // коэффициент умножения для доставки на другие улицы
    private final DeliveryRepository deliveryRepository;
    private final ModelMapper modelMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("Creating new delivery: {}", deliveryDto);
        Delivery newDelivery = modelMapper.map(deliveryDto, Delivery.class);
        newDelivery.setDeliveryState(DeliveryState.CREATED);
        DeliveryDto newDeliveryDto = modelMapper.map(deliveryRepository.save(newDelivery), DeliveryDto.class);
        log.info("New delivery: {} has been created", newDeliveryDto);
        return newDeliveryDto;
    }

    public void deliverySuccessful(UUID orderId) {
        log.info("Creating successful status for order ID: {}", orderId);
        Delivery delivery = getDeliveryByDeliveryId(orderId);
        try {
            orderClient.completed(orderId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoOrderFoundException("Order with ID " + orderId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'warehouse");
        }
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        log.info("Delivery has been completed: {}", delivery);
    }

    public void deliveryPicked(UUID orderId) {
        log.info("Picking delivery with ID: {}", orderId);
        Delivery delivery = getDeliveryByDeliveryId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        try {
            warehouseClient.shippedToDelivery(ShippedToDeliveryRequest.builder()
                    .deliveryId(delivery.getDeliveryId())
                    .orderId(orderId)
                    .build());
        } catch (FeignException ex) {
            throw new RemoteServiceException("Error in the remote service 'warehouse");
        }
        try {
            orderClient.assembly(orderId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoOrderFoundException("Order with ID " + orderId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'warehouse");
        }
        deliveryRepository.save(delivery);
        log.info("Delivery has been picked: {}", delivery);
    }

    public void deliveryFailed(UUID orderId) {
        log.info("Setting delivery status failed to order ID: {}", orderId);
        Delivery delivery = getDeliveryByDeliveryId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        try {
            orderClient.deliveryFailed(orderId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoOrderFoundException("Order with ID " + orderId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'order'");
        }
        deliveryRepository.save(delivery);
        log.info("Delivery status order with ID: {} has been failed", orderId);
    }

    public BigDecimal deliveryCost(OrderDto orderDto) {
        log.info("Calculate delivery cost for order ID: {}", orderDto);
        BigDecimal deliveryCost = BASE_RATE;
        Delivery delivery = getDeliveryByDeliveryId(orderDto.getDeliveryId());
        Address fromAddress = delivery.getFromAddress();
        Address toAddress = delivery.getToAddress();

        if (fromAddress.toString().contains("ADDRESS_1")) {
            deliveryCost = BASE_RATE.multiply(ADDRESS1_RATE);
        } else if (fromAddress.toString().contains("ADDRESS_2")) {
            deliveryCost = deliveryCost.add(BASE_RATE.multiply(ADDRESS2_RATE));
        }

        if (orderDto.isFragile()) {
            deliveryCost = deliveryCost.add(deliveryCost.multiply(FRAGILE_RATE));
        }

        deliveryCost = deliveryCost.add(WEIGHT_RATE.multiply(BigDecimal.valueOf(orderDto.getDeliveryWeight())));
        deliveryCost = deliveryCost.add(VOLUME_RATE.multiply(BigDecimal.valueOf(orderDto.getDeliveryVolume())));

        if (!fromAddress.getStreet().equals(toAddress.getStreet())) {
            deliveryCost = deliveryCost.add(deliveryCost.multiply(STREET_RATE));
        }

        log.info("Delivery cost has been calculated: {}", deliveryCost);
        return deliveryCost;
    }

    private Delivery getDeliveryByDeliveryId(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Not found delivery with ID: " + deliveryId));
    }
}
