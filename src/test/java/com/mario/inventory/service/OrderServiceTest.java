package com.mario.inventory.service;

import com.mario.inventory.domain.Customer;
import com.mario.inventory.domain.OrderLine;
import com.mario.inventory.domain.OrderStatus;
import com.mario.inventory.domain.Product;
import com.mario.inventory.domain.PurchaseOrder;
import com.mario.inventory.domain.Stock;
import com.mario.inventory.domain.Warehouse;
import com.mario.inventory.event.OrderConfirmedEvent;
import com.mario.inventory.exception.BusinessRuleException;
import com.mario.inventory.exception.InsufficientStockException;
import com.mario.inventory.repository.CustomerRepository;
import com.mario.inventory.repository.OrderRepository;
import com.mario.inventory.repository.ProductRepository;
import com.mario.inventory.repository.StockRepository;
import com.mario.inventory.web.dto.OrderDtos.CreateOrderRequest;
import com.mario.inventory.web.dto.OrderDtos.OrderLineRequest;
import com.mario.inventory.web.dto.OrderDtos.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orders;
    @Mock CustomerRepository customers;
    @Mock ProductRepository products;
    @Mock StockRepository stocks;
    @Mock ApplicationEventPublisher events;

    @InjectMocks OrderService service;

    Customer customer;
    Product monitor;
    Warehouse zaragoza;
    Stock stockZaragoza;

    @BeforeEach
    void setUp() {
        customer = withId(new Customer("Talleres Ebro S.L.", "compras@ebro.example", "B50000001"), 1L);
        monitor = withId(new Product("MON-27", "Monitor 27\"", new BigDecimal("289.90"), 2), 10L);
        zaragoza = withId(new Warehouse("ZGZ-01", "Central", "Zaragoza"), 100L);
        stockZaragoza = withId(new Stock(monitor, zaragoza, 5), 1000L);
    }

    @Test
    void createsOrderDiscountingStockAndFreezingPrice() {
        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findById(10L)).thenReturn(Optional.of(monitor));
        when(stocks.findFirstByProductIdAndQuantityGreaterThanEqualOrderByQuantityDesc(10L, 2))
                .thenReturn(Optional.of(stockZaragoza));
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(inv -> withId(inv.getArgument(0), 7L));

        OrderResponse response = service.create(
                new CreateOrderRequest(1L, List.of(new OrderLineRequest(10L, 2))));

        assertThat(stockZaragoza.getQuantity()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.total()).isEqualByComparingTo("579.80");
        assertThat(response.lines()).singleElement()
                .satisfies(line -> assertThat(line.warehouseCode()).isEqualTo("ZGZ-01"));
        verify(events).publishEvent(new OrderConfirmedEvent(7L, List.of(1000L)));
    }

    @Test
    void rejectsOrderWhenNoWarehouseHasEnoughUnits() {
        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findById(10L)).thenReturn(Optional.of(monitor));
        when(stocks.findFirstByProductIdAndQuantityGreaterThanEqualOrderByQuantityDesc(10L, 50))
                .thenReturn(Optional.empty());
        when(stocks.totalQuantity(10L)).thenReturn(5L);

        assertThatThrownBy(() -> service.create(
                new CreateOrderRequest(1L, List.of(new OrderLineRequest(10L, 50)))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("MON-27");

        verify(orders, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    void cancellingReturnsUnitsToTheOriginalWarehouse() {
        PurchaseOrder order = withId(new PurchaseOrder(customer), 7L);
        order.addLine(new OrderLine(monitor, zaragoza, 2, monitor.getPrice()));
        when(orders.findById(7L)).thenReturn(Optional.of(order));
        when(stocks.findByProductIdAndWarehouseId(10L, 100L)).thenReturn(Optional.of(stockZaragoza));

        OrderResponse response = service.cancel(7L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(stockZaragoza.getQuantity()).isEqualTo(7);
    }

    @Test
    void cannotCancelTwice() {
        PurchaseOrder order = withId(new PurchaseOrder(customer), 7L);
        order.cancel();
        when(orders.findById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel(7L)).isInstanceOf(BusinessRuleException.class);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
