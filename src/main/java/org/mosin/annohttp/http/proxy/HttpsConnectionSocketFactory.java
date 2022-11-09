package org.mosin.annohttp.http.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;

/**
 * 用于HTTPS的连接套接字工厂。
 *
 * @author @author Mara.X.Ma
 * @since 1.0.0 2022-07-08
 */
public class HttpsConnectionSocketFactory extends SSLConnectionSocketFactory {

    private static final String REQUEST_PROXY_ID = RequestProxy.class.getName();
    private static final Log LOGGER = LogFactory.getLog(HttpsConnectionSocketFactory.class);

    public HttpsConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
        super(sslContext, hostnameVerifier);
    }

    public HttpsConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        super(sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    public HttpsConnectionSocketFactory(SSLContext sslContext) {
        super(sslContext);
    }

    public HttpsConnectionSocketFactory(SSLSocketFactory socketfactory, HostnameVerifier hostnameVerifier) {
        super(socketfactory, hostnameVerifier);
    }

    public HttpsConnectionSocketFactory(SSLSocketFactory socketfactory, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        super(socketfactory, supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        Socket socket = null;
        RequestProxy requestProxy = getRequestProxy(context);
        if (requestProxy != null && requestProxy.getProxyType() == RequestProxy.ProxyType.SOCKS) {
            // 这里指只处理SOCKS类型的代理
            // 关于HTTP类型的代理由RequestRoutePlanner处理
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
            socket = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(requestProxy.getHost(), requestProxy.getPort())));
        } else {
            socket = SocketFactory.getDefault().createSocket();
        }
        return socket;
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context)
            throws IOException {
        if (getRequestProxy(context) != null) {
            // 如果代理存在的话，让代理服务器去解析主机
            remoteAddress = InetSocketAddress.createUnresolved(host.getHostName(), host.getPort());
        }
        return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
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
