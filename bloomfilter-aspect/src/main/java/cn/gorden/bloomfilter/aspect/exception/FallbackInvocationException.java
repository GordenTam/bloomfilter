package cn.gorden.bloomfilter.aspect.exception;

/**
 * refer to hystrix-javanica
 */
public class FallbackInvocationException extends RuntimeException {

    public FallbackInvocationException() {
    }

    public FallbackInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FallbackInvocationException(Throwable cause) {
        super(cause);
    }
}
