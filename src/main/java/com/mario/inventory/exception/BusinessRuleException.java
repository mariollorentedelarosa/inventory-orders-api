package com.mario.inventory.exception;

/** Petición bien formada pero que incumple una regla de negocio (p. ej. SKU duplicado). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
