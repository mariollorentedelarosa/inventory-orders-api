package com.mario.inventory.web.dto;

import com.mario.inventory.domain.Stock;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class StockDtos {

    private StockDtos() {
    }

    /** Fija la cantidad de un producto en un almacén (inventario / recuento). */
    public record StockRequest(
            @NotNull Long productId,
            @NotNull Long warehouseId,
            @PositiveOrZero int quantity) {
    }

    public record StockResponse(Long id, Long productId, String warehouseCode, int quantity) {

        public static StockResponse from(Stock s) {
            return new StockResponse(s.getId(), s.getProduct().getId(), s.getWarehouse().getCode(), s.getQuantity());
        }
    }
}
