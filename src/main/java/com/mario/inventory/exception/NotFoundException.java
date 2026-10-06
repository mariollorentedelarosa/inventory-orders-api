package com.mario.inventory.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super(resource + " con id " + id + " no existe");
    }
}
