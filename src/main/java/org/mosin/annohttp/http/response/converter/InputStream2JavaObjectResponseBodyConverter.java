package org.mosin.annohttp.http.response.converter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.exception.ConversionException;

public class InputStream2JavaObjectResponseBodyConverter implements ResponseBodyConverter {

    @Override
    public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return ContentType.APPLICATION_OCTET_STREAM.getMimeType().equalsIgnoreCase(computedResponseContentType.getMimeType()) && httpResponse.getEntity() != null && httpResponse.getEntity().isStreaming();
    }

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(httpResponse.getEntity().getContent())) {
            return objectInputStream.readObject();
        } catch (UnsupportedOperationException | IOException | ClassNotFoundException e) {
            throw new ConversionException(this, "Cannot convert response", e);
        }
    }

}
