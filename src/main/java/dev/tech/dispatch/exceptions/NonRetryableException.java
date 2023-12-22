package dev.tech.dispatch.exceptions;

public class NonRetryableException extends RuntimeException {

    public NonRetryableException (String exception) {
        super (exception);
    }

    public NonRetryableException (Exception exception) {
        super (exception);
    }

}
