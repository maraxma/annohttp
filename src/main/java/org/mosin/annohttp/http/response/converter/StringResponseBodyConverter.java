package org.mosin.annohttp.http.response.converter;

import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.exception.ConversionException;

public class StringResponseBodyConverter implements ResponseBodyConverter {

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        try {
            // 这个方法会自动关闭流
            byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
            if (bytes == null) {
                return null;
            }
            return new String(bytes, computedResponseCharset);
        } catch (Exception e) {
            throw new ConversionException(this, "Cannot convert response body to String with charset '" + computedResponseCharset + "'", e);
        }
    }

    @Override
    public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadataContentType, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return metadataContentType.getRequestMethodActualType() instanceof @SuppressWarnings("rawtypes")Class clazz && String.class.isAssignableFrom(clazz)
                && httpResponse.getEntity() != null && httpResponse.getEntity().isStreaming();
    }
}
