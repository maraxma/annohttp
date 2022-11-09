package org.mosin.annohttp.http.request.converter;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.exception.NoApplicableRequestBodyConverterException;

public class AutoRequestBodyConverter implements RequestBodyConverter {

    @Override
    public HttpEntity convert(Object source, ContentType contentType, AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {

        for (RequestBodyConverter requestBodyConverter : RequestBodyConverterCache.getAll().values()) {
            if (requestBodyConverter.canConvert(source, contentType, annoHttpClientMetadata, formFieldName)) {
                return requestBodyConverter.convert(source, contentType, annoHttpClientMetadata, formFieldName);
            }
        }

        throw new NoApplicableRequestBodyConverterException(source, contentType, null);
    }

    @Override
    public boolean canConvert(Object source, ContentType contentType,
                              AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
        return true;
    }

}
