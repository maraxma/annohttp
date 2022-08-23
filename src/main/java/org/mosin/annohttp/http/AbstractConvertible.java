package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;

public abstract class AbstractConvertible implements Convertible {

    protected HttpResponse httpResponse;

    protected AbstractConvertible(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

}
