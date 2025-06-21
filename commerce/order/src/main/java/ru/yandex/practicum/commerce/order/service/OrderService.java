package ru.yandex.practicum.commerce.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.order.OrderState;
import ru.yandex.practicum.commerce.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.exception.*;
import ru.yandex.practicum.commerce.order.feign.DeliveryClient;
import ru.yandex.practicum.commerce.order.feign.PaymentClient;
import ru.yandex.practicum.commerce.order.feign.WarehouseClient;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username, Pageable pageable) {
        log.info("Getting client orders for user: {}", username);
        checkUser(username);
        List<Order> orders = orderRepository.findAllByUsername(username, pageable);
        List<OrderDto> orderDtos = orders.stream().map(order -> modelMapper.map(order, OrderDto.class)).toList();
        log.info("Found {} orders for username: {}", orders.size(), username);
        return orderDtos;
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Creating new order {}", request);

        // сохраним заказ в БД
        Order order = Order.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .state(OrderState.NEW)
                .build();
        order = orderRepository.save(order);

        // отправим заказ на сборку и забронируем товары
        BookedProductsDto bookedProductsDto;
        try {
            AssemblyProductsForOrderRequest assemblyRequest = AssemblyProductsForOrderRequest.builder()
                    .products(request.getShoppingCart().getProducts())
                    .orderId(order.getOrderId())
                    .build();
            bookedProductsDto = warehouseClient.assemblyProductsForOrder(assemblyRequest);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Shopping cart has not passed the stock check");
            } else {
                throw new RemoteServiceException("Error in the remote service 'warehouse'");
            }
        }

        // получим адрес склада
        AddressDto addressWarehouse;
        try {
            addressWarehouse = warehouseClient.getWarehouseAddress();

        } catch (FeignException ex) {
            throw new RemoteServiceException("Error in the remote service 'warehouse'");
        }

        // создадим отправку
        DeliveryDto deliveryDto = DeliveryDto.builder()
                .fromAddress(addressWarehouse)
                .toAddress(request.getDeliveryAddress())
                .orderId(order.getOrderId())
                .deliveryState(DeliveryState.CREATED)
                .build();
        try {
            deliveryDto = deliveryClient.planDelivery(deliveryDto);
        } catch (FeignException ex) {
            throw new RemoteServiceException("Error in the remote service 'delivery'");
        }

        // передадим заказ на оплату
        PaymentDto paymentDto;
        try {
            paymentDto = paymentClient.payment(modelMapper.map(order, OrderDto.class));
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoOrderFoundException("Order with ID " + order.getOrderId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'payment'");
        }

        order.setPaymentId(paymentDto.getPaymentId()); // укажем ID сервиса оплаты
        order.setState(OrderState.ON_PAYMENT); // укажем статус оплаты заказа
        order.setDeliveryId(deliveryDto.getDeliveryId()); // укажем в заказе ID отправки
        order.setDeliveryWeight(bookedProductsDto.getDeliveryWeight()); // укажем вес отправки
        order.setDeliveryVolume(bookedProductsDto.getDeliveryVolume()); // укажем объем отправки
        order.setFragile(bookedProductsDto.getFragile()); // укажем признак хрупкости

        order = orderRepository.save(order); // еще раз запишем заказ в БД
        log.info("New order: {} was created", order);
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Returning products {}", request);
        Order order = getOrderById(request.getOrderId());
        try {
            warehouseClient.acceptReturn(request.getProducts());
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ProductNotFoundException("Some products were not found");
            }
            throw new RemoteServiceException("Error in the remote service 'warehouse'");
        }
        order.setState(OrderState.PRODUCT_RETURNED);
        order = orderRepository.save(order);
        log.info("Products for order ID: {} have been returned", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto payment(UUID orderId) {
        log.info("Paying order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            paymentClient.paymentSuccess(order.getPaymentId());
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoPaymentFoundException("Payment with ID " + order.getPaymentId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'payment'");
        }

        order.setState(OrderState.PAID);
        order = orderRepository.save(order);
        log.info("Order ID: {} was payed", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto paymentFailed(UUID orderId) {
        log.info("Failed paying order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            paymentClient.paymentFailed(order.getPaymentId());
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoPaymentFoundException("Payment with ID " + order.getPaymentId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'payment'");
        }

        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        log.info("Order ID: {} was failed payed", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto delivery(UUID orderId) {
        log.info("Delivering order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            deliveryClient.deliverySuccessful(order.getDeliveryId());
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoDeliveryFoundException("Delivery with ID " + order.getDeliveryId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'delivery'");
        }
        order.setState(OrderState.DELIVERED);
        order = orderRepository.save(order);
        log.info("Order ID: {} was delivered", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Fail delivering order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            deliveryClient.deliverySuccessful(order.getDeliveryId());
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoDeliveryFoundException("Delivery with ID " + order.getDeliveryId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'delivery'");
        }
        order.setState(OrderState.DELIVERY_FAILED);
        order = orderRepository.save(order);
        log.info("Order ID: {} was set fail delivered state", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto completed(UUID orderId) {
        log.info("Competing order ID {}", orderId);
        Order order = getOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        log.info("Order ID: {} was completed", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Calculating total cost for order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            order.setTotalPrice(paymentClient.getTotalCost(modelMapper.map(order, OrderDto.class)));
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoPaymentFoundException("Payment with ID " + order.getPaymentId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'payment'");
        }
        order = orderRepository.save(order);
        log.info("For order ID: {} was calculated total cost", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Calculating delivery cost for order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            order.setDeliveryPrice(deliveryClient.deliveryCost(modelMapper.map(order, OrderDto.class)));
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NoDeliveryFoundException("Delivery with ID " + order.getDeliveryId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'delivery'");
        }
        order = orderRepository.save(order);
        log.info("For order ID: {} was calculated delivery cost", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto assembly(UUID orderId) {
        log.info("Assembling order ID {}", orderId);
        Order order = getOrderById(orderId);
        try {
            AssemblyProductsForOrderRequest assemblyRequest = AssemblyProductsForOrderRequest.builder()
                    .products(order.getProducts())
                    .orderId(order.getOrderId())
                    .build();
            warehouseClient.assemblyProductsForOrder(assemblyRequest);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Shopping cart has not passed the stock check");
            } else {
                throw new RemoteServiceException("Error in the remote service 'warehouse'");
            }
        }
        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);
        log.info("Order ID: {} was assembled", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Fail assembling order ID {}", orderId);
        Order order = getOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        log.info("Order ID: {} was fail assembled", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }

    private void checkUser(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Username is blank");
        }
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Order with ID " + orderId + " not found"));
    }

}
