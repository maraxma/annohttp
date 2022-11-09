package org.mosin.annohttp.http;

import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.proxy.RequestProxy;
import org.mosin.annohttp.http.request.converter.AutoRequestBodyConverter;
import org.mosin.annohttp.http.request.converter.RequestBodyConverter;
import org.mosin.annohttp.http.response.converter.AutoResponseConverter;
import org.mosin.annohttp.http.response.converter.ResponseBodyConverter;

import java.util.List;

/**
 * 直接HTTP客户端接口，提供方便的即时请求。
 * @author Mara.X.Ma
 * @since 1.0.0 2022-09-19
 */
public interface DirectHttpClient {

    <T> T request(HttpMethod httpMethod,
                  String url,
                  ContentType contentType,
                  List<NameValuePair> headers,
                  List<NameValuePair> quires,
                  Object body,
                  RequestProxy requestProxy,
                  Class<? extends RequestBodyConverter> requestBodyConverterClass,
                  Class<? extends ResponseBodyConverter> responseBodyConverterClass);

    <T> PreparingRequest<T> preparing(HttpMethod httpMethod,
                                String url,
                                ContentType contentType,
                                List<NameValuePair> headers,
                                List<NameValuePair> quires,
                                Object body,
                                RequestProxy requestProxy,
                                Class<? extends RequestBodyConverter> requestBodyConverterClass,
                                Class<? extends ResponseBodyConverter> responseBodyConverterClass);

    default <T> T get(String url) {
        return get(url,null,null);
    }

    default <T> T get(String url, List<NameValuePair> headers) {
        return get(url, headers, null);
    }

    default <T> T get(String url, RequestProxy requestProxy) {
        return get(url, null, requestProxy);
    }

    default <T> T get(String url, List<NameValuePair> headers, RequestProxy requestProxy) {
        return request(HttpMethod.GET, url, null, headers, null, null, requestProxy, AutoRequestBodyConverter.class, AutoResponseConverter.class);
    }

    default <T> PreparingRequest<T> getPreparing(String url) {
        return getPreparing(url, null, null);
    }

    default <T> PreparingRequest<T> getPreparing(String url, List<NameValuePair> headers) {
        return getPreparing(url, headers, null);
    }

    default <T> PreparingRequest<T> getPreparing(String url, RequestProxy requestProxy) {
        return getPreparing(url, null, requestProxy);
    }

    default <T> PreparingRequest<T> getPreparing(String url, List<NameValuePair> headers, RequestProxy requestProxy) {
        return preparing(HttpMethod.GET, url, null, headers, null, null, requestProxy, AutoRequestBodyConverter.class, AutoResponseConverter.class);
    }

}
