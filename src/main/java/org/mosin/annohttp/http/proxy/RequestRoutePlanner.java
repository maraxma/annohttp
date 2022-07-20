package org.mosin.annohttp.http.proxy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;

import java.net.ProxySelector;

/**
 * 请求路由计划器。重写DefaultRoutePlanner用于探测自定义的代理设置。
 *
 * @author Mara.X.Ma
 * @since 1.0.0 2022-07-08
 */
public class RequestRoutePlanner extends SystemDefaultRoutePlanner {

    private static final String REQUEST_PROXY_ID = RequestProxy.class.getName();
    private static final Log LOGGER = LogFactory.getLog(RequestRoutePlanner.class);

    public RequestRoutePlanner(ProxySelector proxySelector) {
        super(proxySelector);
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        RequestProxy requestProxy = null;
        if (context instanceof HttpClientProxyContext) {
            requestProxy = ((HttpClientProxyContext) context).getRequestProxy();
        }
        if (requestProxy == null) {
            requestProxy = (RequestProxy) context.getAttribute(REQUEST_PROXY_ID);
        }
        if (requestProxy != null && requestProxy.getProxyType() == RequestProxy.ProxyType.HTTP) {
            // 这里只处理HTTP类型的代理
            LOGGER.debug("Using proxy: " + requestProxy);
            if (requestProxy.withCredential()) {
                HttpClientContext httpClientContext = (HttpClientContext) context;
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                if (requestProxy.getProxyCredentialType() == RequestProxy.ProxyCredentialType.USERNAME_PASSWORD) {
                    credentialsProvider.setCredentials(new AuthScope(requestProxy.getHost(), requestProxy.getPort()),
                            new UsernamePasswordCredentials(requestProxy.getUserName(), requestProxy.getPassword()));
                } else if (requestProxy.getProxyCredentialType() == RequestProxy.ProxyCredentialType.WINDOWS_NT) {
                    credentialsProvider.setCredentials(new AuthScope(requestProxy.getHost(), requestProxy.getPort()),
                            new NTCredentials(requestProxy.getUserName(), requestProxy.getPassword(), requestProxy.getWorkstation(), requestProxy.getDomain()));
                }
                httpClientContext.setCredentialsProvider(credentialsProvider);
            }
            // 构造HttpHost返回
            return new HttpHost(requestProxy.getHost(), requestProxy.getPort());
        }
        return null;
    }

}
