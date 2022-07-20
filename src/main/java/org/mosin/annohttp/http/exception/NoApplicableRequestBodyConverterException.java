package org.mosin.annohttp.http.exception;

import org.apache.http.entity.ContentType;

public class NoApplicableRequestBodyConverterException extends ConversionException {

	private static final long serialVersionUID = -3866209046662394762L;
	
	private ContentType targetContentType;
	private Object source;

	public NoApplicableRequestBodyConverterException() {
		super();
	}

	public NoApplicableRequestBodyConverterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoApplicableRequestBodyConverterException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoApplicableRequestBodyConverterException(String message) {
		super(message);
	}

	public NoApplicableRequestBodyConverterException(Throwable cause) {
		super(cause);
	}
	
	public NoApplicableRequestBodyConverterException(Object source, ContentType targetContentType, Throwable cause) {
		super("Cannot find applicable RequestBodyConverter for target Content-Type '" + targetContentType + "' and source object '" + source + "'", cause);
		this.source = source;
		this.targetContentType = targetContentType;
	}

	public ContentType getTargetContentType() {
		return targetContentType;
	}

	public Object getSource() {
		return source;
	}
	
}
