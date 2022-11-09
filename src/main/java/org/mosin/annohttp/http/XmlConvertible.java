package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public class XmlConvertible extends AbstractJacksonConvertible {

    public XmlConvertible(HttpResponse httpResponse, String charset) {
        super(httpResponse, charset);
        objectMapper = JacksonComponentHolder.getXmlMapper(false, true, true, false);
    }

}
