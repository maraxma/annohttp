package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public class YamlConvertible extends AbstractJacksonConvertible {

    public YamlConvertible(HttpResponse httpResponse, String charset) {
        super(httpResponse, charset);
        objectMapper = JacksonComponentHolder.getYamlMapper(false, true, true, false);
    }
}
