package org.mosin.annohttp.http;

import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.mosin.annohttp.http.proxy.HttpConnectionSocketFactory;
import org.mosin.annohttp.http.proxy.HttpsConnectionSocketFactory;
import org.mosin.annohttp.http.proxy.RequestRoutePlanner;

import java.net.ProxySelector;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * {@link HttpClientBuilder} 增强器，为 ClientBuilder 添加代理支持。
 * @author Mara.X.Ma
 * @since 1.0.0 2022-07-08
 */
public class HttpClientBuilderEnhancer {

    static HttpClientBuilder enhance(HttpClientBuilder clientBuilder) {
        HttpsConnectionSocketFactory sslsf;
        try {
            SSLContextBuilder builder = null;
            builder = new SSLContextBuilder();
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            sslsf = new HttpsConnectionSocketFactory(builder.build(), new String[] {"SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create http client", e);
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new HttpConnectionSocketFactory())
                .register("https", sslsf)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, null, null, null, 15, TimeUnit.SECONDS);
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(2);
        clientBuilder
                .setRoutePlanner(new RequestRoutePlanner(ProxySelector.getDefault()))
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM))
                        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM)).build());

        return clientBuilder;
    }
}
