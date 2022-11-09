package org.mosin.annohttp.http.method;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpTrace;

import java.net.URI;

/**
 * 可携带payload的HttpTrace。
 *
 * @author mm92
 * @since 1.3.1 2019-11-27
 */
public class HttpTraceWithPayload extends HttpEntityEnclosingRequestBase {

    static final String METHOD_NAME = HttpTrace.METHOD_NAME;

    public HttpTraceWithPayload(final URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}