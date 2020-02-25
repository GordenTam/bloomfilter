package org.gorden.bloomfilter.aspect.exception;

/**
 * refer to hystrix-javanica
 */
public class FallbackDefinitionException extends RuntimeException {

    public FallbackDefinitionException() {
    }

    public FallbackDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FallbackDefinitionException(Throwable cause) {
        super(cause);
    }

    public FallbackDefinitionException(String message) {
        super(message);
    }
}
