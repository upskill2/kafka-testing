package dev.tech.dispatch.exceptions;

import org.springframework.web.client.RestClientException;

public class RetryableException extends RuntimeException {
    public RetryableException (String exception) {
        super(exception);
    }

    public RetryableException (Exception exception) {
        super(exception);
    }

}
