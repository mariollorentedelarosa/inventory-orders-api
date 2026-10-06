package com.mario.inventory;

import com.mario.inventory.domain.Customer;
import com.mario.inventory.domain.Product;
import com.mario.inventory.domain.Stock;
import com.mario.inventory.domain.Warehouse;
import com.mario.inventory.exception.InsufficientStockException;
import com.mario.inventory.repository.CustomerRepository;
import com.mario.inventory.repository.ProductRepository;
import com.mario.inventory.repository.StockRepository;
import com.mario.inventory.repository.WarehouseRepository;
import com.mario.inventory.service.OrderService;
import com.mario.inventory.web.dto.OrderDtos.CreateOrderRequest;
import com.mario.inventory.web.dto.OrderDtos.OrderLineRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración contra un PostgreSQL real (Testcontainers).
 *
 * Escenario: queda 1 unidad y llegan N pedidos a la vez. Solo uno puede llevársela;
 * el resto debe fallar limpiamente y el stock nunca puede quedar negativo.
 * Necesita Docker; si no hay Docker disponible, el test se omite.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OrderConcurrencyIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired OrderService orderService;
    @Autowired ProductRepository products;
    @Autowired WarehouseRepository warehouses;
    @Autowired CustomerRepository customers;
    @Autowired StockRepository stocks;

    @Test
    void onlyOneOrderGetsTheLastUnit() throws Exception {
        Product product = products.save(new Product("IT-LAST-UNIT", "Última unidad", new BigDecimal("9.99"), 0));
        Warehouse warehouse = warehouses.save(new Warehouse("IT-WH", "Almacén test", "Zaragoza"));
        stocks.save(new Stock(product, warehouse, 1));
        Customer customer = customers.save(new Customer("Cliente IT", "it-concurrency@example.com", "B00000000"));

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(), List.of(new OrderLineRequest(product.getId(), 1)));

        int buyers = 5;
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(buyers);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < buyers; i++) {
            results.add(pool.submit(() -> {
                startSignal.await();
                try {
                    orderService.create(request);
                    return true;
                } catch (InsufficientStockException | ObjectOptimisticLockingFailureException expected) {
                    return false;
                }
            }));
        }
        startSignal.countDown();

        int successful = 0;
        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) {
                successful++;
            }
        }
        pool.shutdown();

        int remaining = stocks.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseThrow().getQuantity();

        assertThat(successful).isEqualTo(1);
        assertThat(remaining).isZero();
    }
}
