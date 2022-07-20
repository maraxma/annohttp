package org.mosin.annohttp.http.exception;

import java.lang.reflect.Type;

import org.apache.http.entity.ContentType;

public class NoApplicableResponseBodyConverterException extends ConversionException {

    private static final long serialVersionUID = -3866209046662394762L;

    private Type requestMethodReturnType;
    private ContentType contentType;

    public NoApplicableResponseBodyConverterException() {
        super();
    }

    public NoApplicableResponseBodyConverterException(String message, Throwable cause, boolean enableSuppression,
                                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NoApplicableResponseBodyConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoApplicableResponseBodyConverterException(String message) {
        super(message);
    }

    public NoApplicableResponseBodyConverterException(Throwable cause) {
        super(cause);
    }

    public NoApplicableResponseBodyConverterException(Type requestMethodReturnType, ContentType contentType, Throwable cause) {
        super("Cannot find applicable ResponseBodyConverter for target Type '" + requestMethodReturnType + "' with content type '" + contentType + "'", cause);
        this.requestMethodReturnType = requestMethodReturnType;
        this.contentType = contentType;
    }

    public Type getRequestMethodReturnType() {
        return requestMethodReturnType;
    }

    public ContentType getContentType() {
        return contentType;
    }

}
