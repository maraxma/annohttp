package org.mosin.annohttp.http.response.converter;

import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;

public class HttpResponseResponseBodyConverter implements ResponseBodyConverter {

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return httpResponse;
    }

    @Override
    public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return metadata.getRequestMethodActualType() instanceof @SuppressWarnings("rawtypes")Class clazz && HttpResponse.class.isAssignableFrom(clazz);
    }

}
