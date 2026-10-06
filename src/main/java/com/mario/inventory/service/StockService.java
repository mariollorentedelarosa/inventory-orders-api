package com.mario.inventory.service;

import com.mario.inventory.domain.Product;
import com.mario.inventory.domain.Stock;
import com.mario.inventory.domain.Warehouse;
import com.mario.inventory.exception.NotFoundException;
import com.mario.inventory.repository.ProductRepository;
import com.mario.inventory.repository.StockRepository;
import com.mario.inventory.repository.WarehouseRepository;
import com.mario.inventory.web.dto.StockDtos.StockRequest;
import com.mario.inventory.web.dto.StockDtos.StockResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stocks;
    private final ProductRepository products;
    private final WarehouseRepository warehouses;

    public StockService(StockRepository stocks, ProductRepository products, WarehouseRepository warehouses) {
        this.stocks = stocks;
        this.products = products;
        this.warehouses = warehouses;
    }

    public List<StockResponse> byProduct(Long productId) {
        if (!products.existsById(productId)) {
            throw new NotFoundException("Producto", productId);
        }
        return stocks.findByProductIdWithWarehouse(productId).stream().map(StockResponse::from).toList();
    }

    /** Crea el registro de stock si no existe o fija su cantidad si ya existe. */
    @Transactional
    public StockResponse set(StockRequest request) {
        Stock stock = stocks.findByProductIdAndWarehouseId(request.productId(), request.warehouseId())
                .orElseGet(() -> {
                    Product product = products.findById(request.productId())
                            .orElseThrow(() -> new NotFoundException("Producto", request.productId()));
                    Warehouse warehouse = warehouses.findById(request.warehouseId())
                            .orElseThrow(() -> new NotFoundException("Almacén", request.warehouseId()));
                    return stocks.save(new Stock(product, warehouse, 0));
                });
        stock.adjustTo(request.quantity());
        return StockResponse.from(stock);
    }
}
