package ru.yandex.practicum.commerce.dto.payment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Информация об оплате
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDto {
    UUID paymentId; // идентификатор оплаты

    BigDecimal totalPayment; // общая стоимость

    BigDecimal deliveryTotal; // стоимость доставки

    BigDecimal feeTotal; // стоимость налога
}
