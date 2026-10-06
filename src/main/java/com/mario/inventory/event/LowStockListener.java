package com.mario.inventory.event;

import com.mario.inventory.domain.Stock;
import com.mario.inventory.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Comprueba el stock mínimo después de cada pedido.
 *
 * AFTER_COMMIT: solo se ejecuta si el pedido se guardó de verdad.
 * @Async: no retrasa la respuesta al cliente.
 * Siguiente paso: sustituir el log por un email o un mensaje a una cola (RabbitMQ/Kafka).
 */
@Component
public class LowStockListener {

    private static final Logger log = LoggerFactory.getLogger(LowStockListener.class);

    private final StockRepository stocks;

    public LowStockListener(StockRepository stocks) {
        this.stocks = stocks;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        stocks.findWithDetailsByIdIn(event.stockIds()).stream()
                .filter(Stock::isBelowMinimum)
                .forEach(s -> log.warn("Stock bajo tras pedido {}: {} en {} -> quedan {} uds (mínimo {})",
                        event.orderId(),
                        s.getProduct().getSku(),
                        s.getWarehouse().getCode(),
                        s.getQuantity(),
                        s.getProduct().getMinStock()));
    }
}
