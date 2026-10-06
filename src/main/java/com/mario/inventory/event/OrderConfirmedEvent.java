package com.mario.inventory.event;

import java.util.List;

/** Se publica al confirmar un pedido. stockIds = registros de stock que se han descontado. */
public record OrderConfirmedEvent(Long orderId, List<Long> stockIds) {
}
