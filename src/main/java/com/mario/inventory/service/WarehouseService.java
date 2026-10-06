package com.mario.inventory.service;

import com.mario.inventory.domain.Warehouse;
import com.mario.inventory.exception.BusinessRuleException;
import com.mario.inventory.repository.WarehouseRepository;
import com.mario.inventory.web.dto.WarehouseDtos.WarehouseRequest;
import com.mario.inventory.web.dto.WarehouseDtos.WarehouseResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouses;

    public WarehouseService(WarehouseRepository warehouses) {
        this.warehouses = warehouses;
    }

    public List<WarehouseResponse> list() {
        return warehouses.findAll(Sort.by("code")).stream().map(WarehouseResponse::from).toList();
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        if (warehouses.existsByCode(request.code())) {
            throw new BusinessRuleException("Ya existe un almacén con código " + request.code());
        }
        return WarehouseResponse.from(warehouses.save(new Warehouse(request.code(), request.name(), request.city())));
    }
}
