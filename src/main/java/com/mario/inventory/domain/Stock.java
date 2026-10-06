package com.mario.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Unidades de un producto en un almacén concreto.
 *
 * El campo {@code version} activa el bloqueo optimista: si dos pedidos leen el mismo
 * stock a la vez, solo el primero en confirmar gana; el segundo recibe un
 * ObjectOptimisticLockingFailureException en lugar de vender unidades que ya no existen.
 */
@Entity
@Table(name = "stock",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_product_warehouse",
                columnNames = {"product_id", "warehouse_id"}))
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(nullable = false)
    private int quantity;

    @Version
    private long version;

    protected Stock() {
    }

    public Stock(Product product, Warehouse warehouse, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
    }

    public void decrease(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Las unidades deben ser positivas");
        }
        if (units > quantity) {
            throw new IllegalStateException("Stock insuficiente en el almacén " + warehouse.getCode());
        }
        quantity -= units;
    }

    public void increase(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Las unidades deben ser positivas");
        }
        quantity += units;
    }

    public void adjustTo(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        quantity = newQuantity;
    }

    public boolean isBelowMinimum() {
        return quantity < product.getMinStock();
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public Warehouse getWarehouse() { return warehouse; }
    public int getQuantity() { return quantity; }
    public long getVersion() { return version; }
}
