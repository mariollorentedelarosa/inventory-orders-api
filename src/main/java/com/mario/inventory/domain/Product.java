package com.mario.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Por debajo de esta cantidad en un almacén se lanza un aviso de reposición. */
    @Column(name = "min_stock", nullable = false)
    private int minStock;

    protected Product() {
    }

    public Product(String sku, String name, BigDecimal price, int minStock) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.minStock = minStock;
    }

    public void update(String name, BigDecimal price, int minStock) {
        this.name = name;
        this.price = price;
        this.minStock = minStock;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getMinStock() { return minStock; }
}
