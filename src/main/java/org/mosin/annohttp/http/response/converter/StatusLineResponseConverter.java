package org.mosin.annohttp.http.response.converter;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;

import java.nio.charset.Charset;

/**
 * StatusLine转换器。负责提取响应体中的StatusLine。
 * @author Mara.X.Ma
 * @since 1.0.0 2022-10-11
 */
public class StatusLineResponseConverter implements ResponseConverter {

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return httpResponse.getStatusLine();
    }

    @Override
    public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        return metadata.getRequestMethodActualType() instanceof @SuppressWarnings("rawtypes")Class clazz && StatusLine.class.isAssignableFrom(clazz);
    }

}
