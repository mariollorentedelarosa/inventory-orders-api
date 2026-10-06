package com.mario.inventory.web.dto;

import com.mario.inventory.domain.Warehouse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class WarehouseDtos {

    private WarehouseDtos() {
    }

    public record WarehouseRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String city) {
    }

    public record WarehouseResponse(Long id, String code, String name, String city) {

        public static WarehouseResponse from(Warehouse w) {
            return new WarehouseResponse(w.getId(), w.getCode(), w.getName(), w.getCity());
        }
    }
}
