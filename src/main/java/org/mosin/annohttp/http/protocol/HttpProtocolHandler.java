package org.mosin.annohttp.http.protocol;

import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.PreparingRequest;

public final class HttpProtocolHandler implements ProtocolHandler {

    @Override
    public String protocol() {
        return "http" + PROTOCOL_SPLITTER +  "https";
    }

    @Override
    public void handle(AnnoHttpClientMetadata metadata, PreparingRequest<?> preparingRequest) {

    }
}
