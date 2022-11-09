package org.mosin.annohttp.spring.configuration;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.mosin.annohttp.http.proxy.HttpConnectionSocketFactory;
import org.mosin.annohttp.http.proxy.HttpsConnectionSocketFactory;
import org.mosin.annohttp.http.proxy.RequestRoutePlanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.ProxySelector;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties({AnnoHttpProperties.class})
public class AnnoHttpConfiguration {

    @Bean
    public HttpClientBuilder httpClientBuilder(AnnoHttpProperties properties) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry;
        if (properties.isTrustAnySsl()) {
            HttpsConnectionSocketFactory sslsf = null;
            try {
                SSLContextBuilder builder = null;
                builder = new SSLContextBuilder();
                // 全部信任 不做身份鉴定
                builder.loadTrustMaterial(null, (chain, authType) -> true);
                sslsf = new HttpsConnectionSocketFactory(builder.build(), new String[] {"SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create http client", e);
            }
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new HttpConnectionSocketFactory())
                    .register("https", sslsf)
                    .build();
        } else {
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", SSLConnectionSocketFactory.getSocketFactory())
                    .build();
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, null, null, null, properties.getConnectionIdleTimeoutInSeconds(), TimeUnit.SECONDS);
        connectionManager.setMaxTotal(properties.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(properties.getMaxConnectionsPerRoute());
        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setRoutePlanner(new RequestRoutePlanner(ProxySelector.getDefault()))
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM))
                        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM))
                        .setSocketTimeout(properties.getSocketTimeoutInSeconds() * 1000)
                        .setConnectTimeout(properties.getConnectTimeoutInSeconds() * 1000)
                        .setRedirectsEnabled(properties.isFlowRedirect())
                        .build());

        if (properties.isKeepAlive()) {
            clientBuilder
                    .setKeepAliveStrategy((HttpResponse response, HttpContext context) -> properties.getKeepAliveTimeInSeconds() * 1000L)
                    .setDefaultHeaders(Arrays.asList(new BasicHeader("Connection", "keep-alive"), new BasicHeader("Keep-Alive", "timeout=" + properties.getKeepAliveTimeInSeconds())));
        }
        return clientBuilder;
    }
}
