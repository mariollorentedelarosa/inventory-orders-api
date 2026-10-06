package com.mario.inventory.web.dto;

import com.mario.inventory.domain.OrderLine;
import com.mario.inventory.domain.OrderStatus;
import com.mario.inventory.domain.PurchaseOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotNull Long customerId,
            @NotEmpty List<@Valid OrderLineRequest> lines) {
    }

    public record OrderLineRequest(
            @NotNull Long productId,
            @Positive int quantity) {
    }

    /** Resumen para listados (sin líneas, para no cargar todo en cada página). */
    public record OrderSummary(Long id, String customerName, OrderStatus status, Instant createdAt, BigDecimal total) {

        public static OrderSummary from(PurchaseOrder o) {
            return new OrderSummary(o.getId(), o.getCustomer().getName(), o.getStatus(), o.getCreatedAt(), o.getTotal());
        }
    }

    public record OrderResponse(Long id, Long customerId, String customerName, OrderStatus status,
                                Instant createdAt, BigDecimal total, List<OrderLineResponse> lines) {

        public static OrderResponse from(PurchaseOrder o) {
            return new OrderResponse(
                    o.getId(),
                    o.getCustomer().getId(),
                    o.getCustomer().getName(),
                    o.getStatus(),
                    o.getCreatedAt(),
                    o.getTotal(),
                    o.getLines().stream().map(OrderLineResponse::from).toList());
        }
    }

    public record OrderLineResponse(String sku, String productName, String warehouseCode,
                                    int quantity, BigDecimal unitPrice, BigDecimal subtotal) {

        public static OrderLineResponse from(OrderLine l) {
            return new OrderLineResponse(
                    l.getProduct().getSku(),
                    l.getProduct().getName(),
                    l.getWarehouse().getCode(),
                    l.getQuantity(),
                    l.getUnitPrice(),
                    l.subtotal());
        }
    }
}
