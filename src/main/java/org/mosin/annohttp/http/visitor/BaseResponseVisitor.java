package org.mosin.annohttp.http.visitor;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * 
 * @author MARA
 *
 */
public class BaseResponseVisitor implements ResponseVisitor {

    @Override
    public void visit(HttpClientBuilder httpClientBuilder, CloseableHttpClient httpClient, Object serviceClient, HttpResponse response, Throwable exception) throws Throwable {
        if (exception != null) {
            try {
                onError(httpClientBuilder, httpClient, serviceClient, exception);
            } catch (Exception e) {
                throw e;
            }
            // 无论如何，确保有异常向上抛出
            throw exception;
        }
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            onNot2xxResponse(httpClientBuilder, httpClient, serviceClient, response);
        } else {
            on2xxResponse(httpClientBuilder, httpClient, serviceClient, response);
        }
        onComplete(httpClientBuilder, httpClient, serviceClient, response);
    }

    /**
     * 当请求发生错误时。
     * @param httpClientBuilder HttpClient建造者
     * @param httpClient 发起此次访问的HTTP客户端
     * @param serviceClient 发起此次访问的服务客户端
     * @param exception 发起请求过程中产生的错误
     */
    protected void onError(HttpClientBuilder httpClientBuilder, CloseableHttpClient httpClient, Object serviceClient, Throwable exception) {

    }

    /**
     * 当请求返回的是非2XX响应码时。
     * @param httpClientBuilder HttpClient建造者
     * @param httpClient 发起此次访问的HTTP客户端
     * @param serviceClient 发起此次访问的服务客户端
     * @param response 响应
     */
    protected void onNot2xxResponse(HttpClientBuilder httpClientBuilder, CloseableHttpClient httpClient, Object serviceClient, HttpResponse response) {

    }

    /**
     * 当请求返回的是2XX响应码时。
     * @param httpClientBuilder HttpClient建造者
     * @param httpClient 发起此次访问的HTTP客户端
     * @param serviceClient 发起此次访问的服务客户端
     * @param response 响应
     */
    protected void on2xxResponse(HttpClientBuilder httpClientBuilder, CloseableHttpClient httpClient, Object serviceClient, HttpResponse response) {

    }

    /**
     * 当请求成功完成时（只要有响应就算成功完成，无论响应的内容是什么，也无论状态码是什么）。
     * @param httpClientBuilder HttpClient建造者
     * @param httpClient 发起此次访问的HTTP客户端
     * @param serviceClient 发起此次访问的服务客户端
     * @param response 响应，如果存在exception，那么response是null
     */
    protected void onComplete(HttpClientBuilder httpClientBuilder, CloseableHttpClient httpClient, Object serviceClient, HttpResponse response) {

    }
}
