package org.mosin.annohttp.http.response.converter;

import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.exception.NoApplicableResponseBodyConverterException;

/**
 * 自动识别的响应转换器。
 */
public class AutoResponseBodyConverter implements ResponseBodyConverter {

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {

        for (ResponseBodyConverter responseBodyConverter : ResponseBodyConverterCache.getAll().values()) {
            if (responseBodyConverter.canConvert(httpResponse, metadata, computedResponseContentType, computedResponseCharset)) {
                return responseBodyConverter.convert(httpResponse, metadata, computedResponseContentType, computedResponseCharset);
            }
        }

        // 找不到合适的转换器，那么直接抛出异常，提醒用户自己创建
        throw new NoApplicableResponseBodyConverterException(metadata.getRequestMethodActualType(), computedResponseContentType, null);
    }

    @Override
    public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return true;
    }
}
