package org.mosin.annohttp.http.proxy;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;

import org.mosin.annohttp.http.proxy.RequestProxy.ProxyCredentialType;
import org.mosin.annohttp.http.proxy.RequestProxy.ProxyType;

/**
 * 用于HTTP的连接套接字工厂。
 *
 * @author @author Mara.X.Ma
 * @since 1.0.0 2022-07-08
 */
public class HttpConnectionSocketFactory extends PlainConnectionSocketFactory {

    private static final String REQUEST_PROXY_ID = RequestProxy.class.getName();
    private static final Log LOGGER = LogFactory.getLog(HttpConnectionSocketFactory.class);

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        Socket socket;
        RequestProxy requestProxy = getRequestProxy(context);
        if (requestProxy != null && requestProxy.getProxyType() == ProxyType.SOCKS) {
            // 这里指只处理SOCKS类型的代理
            // 关于HTTP类型的代理由RequestRoutePlanner处理
            LOGGER.debug("Using proxy: " + requestProxy);
            if (requestProxy.withCredential()) {
                HttpClientContext httpClientContext = (HttpClientContext) context;
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                if (requestProxy.getProxyCredentialType() == ProxyCredentialType.USERNAME_PASSWORD) {
                    credentialsProvider.setCredentials(new AuthScope(requestProxy.getHost(), requestProxy.getPort()),
                            new UsernamePasswordCredentials(requestProxy.getUserName(), requestProxy.getPassword()));
                } else if (requestProxy.getProxyCredentialType() == ProxyCredentialType.WINDOWS_NT) {
                    credentialsProvider.setCredentials(new AuthScope(requestProxy.getHost(), requestProxy.getPort()),
                            new NTCredentials(requestProxy.getUserName(), requestProxy.getPassword(), requestProxy.getWorkstation(), requestProxy.getDomain()));
                }
                httpClientContext.setCredentialsProvider(credentialsProvider);
            }
            socket = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(requestProxy.getHost(), requestProxy.getPort())));
        } else {
            socket = new Socket();
        }
        return socket;
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context)
            throws IOException {
        RequestProxy requestProxy = getRequestProxy(context);
        if (requestProxy != null && requestProxy.getProxyType() == ProxyType.SOCKS) {
            // 如果代理存在的话，让代理服务器去解析主机
            remoteAddress = InetSocketAddress.createUnresolved(host.getHostName(), host.getPort());
        }
        return super.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
    }

    private RequestProxy getRequestProxy(HttpContext context) {
        RequestProxy requestProxy = null;
        if (context instanceof HttpClientProxyContext) {
            requestProxy = ((HttpClientProxyContext) context).getRequestProxy();
        }
        if (requestProxy == null) {
            requestProxy = (RequestProxy) context.getAttribute(REQUEST_PROXY_ID);
        }
        return requestProxy;
    }

}
