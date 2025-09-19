package it.fabiodezuani.queryengine.exception;
/**
 * Eccezione personalizzata per errori nella creazione delle Specification
 */
public class SpecificationException extends RuntimeException {

    public SpecificationException(String message) {
        super(message);
    }

    public SpecificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
