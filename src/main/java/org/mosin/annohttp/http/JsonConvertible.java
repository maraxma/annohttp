package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public class JsonConvertible extends AbstractJacksonConvertible {

    protected JsonConvertible(HttpResponse httpResponse, String charset) {
        super(httpResponse, charset);
        this.objectMapper = JacksonComponentHolder.getJsonMapper(false, true, true, false);
    }
}
