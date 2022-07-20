package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public abstract class AbstractConvertable implements Convertable {
	
	protected HttpResponse httpResponse;

	protected AbstractConvertable(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}
	
}
