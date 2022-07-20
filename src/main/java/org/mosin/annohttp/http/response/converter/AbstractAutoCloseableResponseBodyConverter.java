package org.mosin.annohttp.http.response.converter;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class AbstractAutoCloseableResponseBodyConverter implements ResponseBodyConverter, Closeable {

    private HttpResponse httpResponse;

    @Override
    public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset) {
        try {
            this.httpResponse = httpResponse;
            return doConvert(httpResponse, metadata, computedResponseContentType, computedResponseCharset);
        } finally {
            try {
                close();
            } catch (IOException e) {
                // IGNORE
            }
        }
    }

    @Override
    public void close() throws IOException {
        EntityUtils.consumeQuietly(httpResponse.getEntity());
    }

    protected abstract Object doConvert(HttpResponse httpResponse, AnnoHttpClientMetadata annoHttpClientMetadata, ContentType computedResponseContentType, Charset computedResponseCharset);
}
