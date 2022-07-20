package org.mosin.annohttp.http.response.converter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.exception.ConversionException;

public class InputStreamResponseBodyConverter implements ResponseBodyConverter {

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return Optional.ofNullable(httpResponse.getEntity()).map(entity -> {
            try {
                return entity.getContent();
            } catch (IOException e) {
                throw new ConversionException(this, "Cannot convert response body to InputStream", e);
            }
        }).orElse(null);
    }

	@Override
	public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
		return metadata.getRequestMethodActualType() instanceof @SuppressWarnings("rawtypes") Class clazz && InputStream.class.isAssignableFrom(clazz)
				&& httpResponse.getEntity() != null && httpResponse.getEntity().isStreaming();
	}

}
