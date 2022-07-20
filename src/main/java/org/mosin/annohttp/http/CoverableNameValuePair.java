package org.mosin.annohttp.http;

import org.apache.http.message.BasicNameValuePair;

public class CoverableNameValuePair extends BasicNameValuePair {

    private static final long serialVersionUID = -6809721642742357316L;

    private boolean coverable;

    public CoverableNameValuePair(String name, String value, boolean coverable) {
        super(name, value);
        this.coverable = coverable;
    }

    public CoverableNameValuePair(String name, String value) {
        this(name, value, false);
    }

    public boolean isCoverable() {
        return coverable;
    }
}
