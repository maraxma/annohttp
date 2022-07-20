package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public class XmlConvertable extends AbstractJacksonConvertable {

	public XmlConvertable(HttpResponse httpResponse, String charset) {
		super(httpResponse, charset);
		objectMapper = JacksonComponentHolder.getXmlMapper(false, true, true, false);
	}
	
}
