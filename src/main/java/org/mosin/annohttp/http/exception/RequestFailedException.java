package org.mosin.annohttp.http.exception;

public class RequestFailedException extends RuntimeException {

    private static final long serialVersionUID = 3389270448584832066L;

    public RequestFailedException() {
        super();
    }

    public RequestFailedException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestFailedException(String message) {
        super(message);
    }

    public RequestFailedException(Throwable cause) {
        super(cause);
    }

}
