package org.mosin.annohttp.http.method;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpHead;

import java.net.URI;

/**
 * 可携带payload的HttpHead。
 *
 * @author mm92
 * @since 1.3.1 2019-11-27
 */
public class HttpHeadWithPayload extends HttpEntityEnclosingRequestBase {

    static final String METHOD_NAME = HttpHead.METHOD_NAME;

    public HttpHeadWithPayload(final URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}