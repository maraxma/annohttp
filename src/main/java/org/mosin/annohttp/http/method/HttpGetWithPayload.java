package org.mosin.annohttp.http.method;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

/**
 * 可携带payload的HttpGet。
 *
 * @author Mara.X.Ma
 * @since 1.0.0 2022-07-16
 */
public class HttpGetWithPayload extends HttpEntityEnclosingRequestBase {

    static final String METHOD_NAME = HttpGet.METHOD_NAME;

    public HttpGetWithPayload(final URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}