package com.mario.inventory.service;

import com.mario.inventory.domain.Customer;
import com.mario.inventory.domain.OrderLine;
import com.mario.inventory.domain.OrderStatus;
import com.mario.inventory.domain.Product;
import com.mario.inventory.domain.PurchaseOrder;
import com.mario.inventory.domain.Stock;
import com.mario.inventory.event.OrderConfirmedEvent;
import com.mario.inventory.exception.BusinessRuleException;
import com.mario.inventory.exception.InsufficientStockException;
import com.mario.inventory.exception.NotFoundException;
import com.mario.inventory.repository.CustomerRepository;
import com.mario.inventory.repository.OrderRepository;
import com.mario.inventory.repository.ProductRepository;
import com.mario.inventory.repository.StockRepository;
import com.mario.inventory.web.dto.OrderDtos.CreateOrderRequest;
import com.mario.inventory.web.dto.OrderDtos.OrderLineRequest;
import com.mario.inventory.web.dto.OrderDtos.OrderResponse;
import com.mario.inventory.web.dto.OrderDtos.OrderSummary;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final StockRepository stocks;
    private final ApplicationEventPublisher events;

    public OrderService(OrderRepository orders, CustomerRepository customers, ProductRepository products,
                        StockRepository stocks, ApplicationEventPublisher events) {
        this.orders = orders;
        this.customers = customers;
        this.products = products;
        this.stocks = stocks;
        this.events = events;
    }

    /**
     * Crea y confirma un pedido en una única transacción: o se reserva el stock de todas las
     * líneas y se guarda el pedido, o no se toca nada.
     *
     * Cada línea sale del almacén con más unidades que pueda servirla entera.
     */
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Cliente", request.customerId()));

        PurchaseOrder order = new PurchaseOrder(customer);
        List<Long> touchedStock = new ArrayList<>();

        for (OrderLineRequest line : request.lines()) {
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("Producto", line.productId()));

            Stock stock = stocks
                    .findFirstByProductIdAndQuantityGreaterThanEqualOrderByQuantityDesc(product.getId(), line.quantity())
                    .orElseThrow(() -> new InsufficientStockException(
                            product.getSku(), line.quantity(), stocks.totalQuantity(product.getId())));

            stock.decrease(line.quantity());
            order.addLine(new OrderLine(product, stock.getWarehouse(), line.quantity(), product.getPrice()));
            touchedStock.add(stock.getId());
        }

        PurchaseOrder saved = orders.save(order);
        events.publishEvent(new OrderConfirmedEvent(saved.getId(), touchedStock));
        return OrderResponse.from(saved);
    }

    /** Cancela el pedido y devuelve las unidades al almacén del que salieron. */
    @Transactional
    public OrderResponse cancel(Long id) {
        PurchaseOrder order = find(id);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("El pedido " + id + " ya está " + order.getStatus());
        }
        for (OrderLine line : order.getLines()) {
            Stock stock = stocks.findByProductIdAndWarehouseId(line.getProduct().getId(), line.getWarehouse().getId())
                    .orElseThrow(() -> new IllegalStateException("Falta el registro de stock de la línea " + line.getId()));
            stock.increase(line.getQuantity());
        }
        order.cancel();
        return OrderResponse.from(order);
    }

    public OrderResponse get(Long id) {
        return OrderResponse.from(find(id));
    }

    public Page<OrderSummary> list(Long customerId, Pageable pageable) {
        Page<PurchaseOrder> page = customerId == null
                ? orders.findAllBy(pageable)
                : orders.findByCustomerId(customerId, pageable);
        return page.map(OrderSummary::from);
    }

    private PurchaseOrder find(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Pedido", id));
    }
}
