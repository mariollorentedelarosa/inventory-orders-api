package com.mario.inventory.exception;

public class InsufficientStockException extends RuntimeException {

    private final String sku;
    private final int requested;
    private final long available;

    public InsufficientStockException(String sku, int requested, long available) {
        super("No hay ningún almacén con " + requested + " uds de " + sku
                + " (total disponible entre todos los almacenes: " + available + ")");
        this.sku = sku;
        this.requested = requested;
        this.available = available;
    }

    public String getSku() { return sku; }
    public int getRequested() { return requested; }
    public long getAvailable() { return available; }
}
