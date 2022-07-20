package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public class YamlConvertable extends AbstractJacksonConvertable {

    public YamlConvertable(HttpResponse httpResponse, String charset) {
        super(httpResponse, charset);
        objectMapper = JacksonComponentHolder.getYamlMapper(false, true, true, false);
    }
}
