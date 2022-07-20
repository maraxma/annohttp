package org.mosin.annohttp.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public final class HttpComponentHolder {

    private static volatile HttpClientBuilder httpClientBuilder;
    private static volatile CloseableHttpClient httpClient;

    private static final Object HTTP_CLIENT_BUILDER_LOCK = new Object();
    private static final Object HTTP_CLIENT_LOCK = new Object();

    public static HttpClientBuilder getHttpClientBuilderInstance() {
        if (httpClientBuilder == null) {
            synchronized (HTTP_CLIENT_BUILDER_LOCK) {
                if (httpClientBuilder == null) {
                    httpClientBuilder = HttpClientBuilderEnhancer.enhance(HttpClientBuilder.create());
                }
            }
        }
        return httpClientBuilder;
    }

    public static CloseableHttpClient getHttpClientInstance() {
        if (httpClient == null) {
            synchronized (HTTP_CLIENT_LOCK) {
                if (httpClient == null) {
                    httpClient = getHttpClientBuilderInstance().build();
                }
            }
        }
        return httpClient;
    }
}
