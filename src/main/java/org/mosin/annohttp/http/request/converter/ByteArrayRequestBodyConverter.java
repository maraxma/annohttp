package org.mosin.annohttp.http.request.converter;

import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;

public class ByteArrayRequestBodyConverter implements RequestBodyConverter {

    @Override
    public HttpEntity convert(Object source, ContentType contentType,
                              AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
        HttpEntity httpEntity;
        if (contentType == null) {
            httpEntity = new ByteArrayEntity((byte[]) source, ContentType.APPLICATION_OCTET_STREAM);
        } else {
            if (ContentType.TEXT_PLAIN.getMimeType().equalsIgnoreCase(contentType.getMimeType())) {
                Charset charset = contentType.getCharset();
                charset = charset == null ? DEFAULT_STRING_CHARSET : charset;
                httpEntity = new StringEntity(new String((byte[]) source, charset), charset);
            } else {
                throw new IllegalArgumentException("If @Body represent a byte[], its content type can application/octet-stream or text/plain only, or you can not set content type, annohhttp set it as application/octet-stream by default");
            }
        }

        return httpEntity;
    }

    @Override
    public boolean canConvert(Object source, ContentType contentType,
                              AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
        return source instanceof byte[];
    }

}
