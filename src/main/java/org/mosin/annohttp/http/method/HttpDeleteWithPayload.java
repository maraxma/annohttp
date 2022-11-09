package org.mosin.annohttp.http.method;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * 可携带payload的HttpDelete。
 *
 * @author mm92
 * @since 1.3.1 2019-11-27
 */
public class HttpDeleteWithPayload extends HttpEntityEnclosingRequestBase {

    static final String METHOD_NAME = HttpDelete.METHOD_NAME;

    public HttpDeleteWithPayload(final URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}