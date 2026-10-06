package com.mario.inventory.web;

import com.mario.inventory.service.StockService;
import com.mario.inventory.web.dto.StockDtos.StockRequest;
import com.mario.inventory.web.dto.StockDtos.StockResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public List<StockResponse> byProduct(@RequestParam Long productId) {
        return service.byProduct(productId);
    }

    @PutMapping
    public StockResponse set(@Valid @RequestBody StockRequest request) {
        return service.set(request);
    }
}
