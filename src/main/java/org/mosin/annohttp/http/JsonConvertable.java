package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public class JsonConvertable extends AbstractJacksonConvertable {

	protected JsonConvertable(HttpResponse httpResponse, String charset) {
		super(httpResponse, charset);
		this.objectMapper = JacksonComponentHolder.getJsonMapper(false, true, true, false);
	}
}
