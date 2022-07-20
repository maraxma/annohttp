package org.mosin.annohttp.http.method;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpOptions;

import java.net.URI;

/**
 * 可携带payload的HttpOptions。
 *
 * @author mm92
 * @since 1.3.1 2019-11-27
 */
public class HttpOptionsWithPayload extends HttpEntityEnclosingRequestBase {

    static final String METHOD_NAME = HttpOptions.METHOD_NAME;

    public HttpOptionsWithPayload(final URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}