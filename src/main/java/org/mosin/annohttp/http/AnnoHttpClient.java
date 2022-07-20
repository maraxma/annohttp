package org.mosin.annohttp.http;

import java.lang.reflect.Proxy;

import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.request.converter.AutoRequestBodyConverter;
import org.mosin.annohttp.http.request.converter.RequestBodyConverter;
import org.mosin.annohttp.http.request.converter.RequestBodyConverterCache;
import org.mosin.annohttp.http.response.converter.AutoResponseBodyConverter;
import org.mosin.annohttp.http.response.converter.ResponseBodyConverter;
import org.mosin.annohttp.http.response.converter.ResponseBodyConverterCache;

/**
 * AnnoHttp的主要入口API，负责帮助用户创建请求实例。
 *
 * <p>创建一个注解驱动的请求实例需要如下两步：</p>
 * <ol>
 *    <li>创建接口，定义请求方法，并在请求方法上使用annohttp提供的注解申明请求行为；</li>
 *    <li>使用 {@link AnnoHttpClient#create(Class)} 方法传入接口类，创建真正的实例。</li>
 * </ol>
 *
 * @author Mara.X.Ma
 * @since 1.0.0 2022-07-08
 */
public final class AnnoHttpClient {

    /**
     * 为请求接口创建具体的实例。创建好后，你将可以直接调用。
     *
     * @param <T>           实例接口类型
     * @param annoHttpClass 请求接口类
     * @return 接口实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> annoHttpClass) {
        // 动态代理
        // 都是基于接口的，因此使用JDK自带的动态代理即可
        return (T) Proxy.newProxyInstance(annoHttpClass.getClassLoader(), new Class<?>[]{annoHttpClass}, InvocationHandlerHolder.INSTANCE);
    }

    /**
     * 注册请求体转换器。你可以定义自己的转换器，然后使用默认的 {@link AutoRequestBodyConverter} 来查找并应用。
     * <p>你也可以直接将自己的转换器写到 {@link Request#requestBodyConverter()} 上。
     *
     * @param requestBodyConverters 请求体转换器
     */
    public static void registerRequestBodyConverter(RequestBodyConverter... requestBodyConverters) {
        RequestBodyConverterCache.addUserConverters(requestBodyConverters);
    }

    /**
     * 注册响应体转换器。你可以定义自己的转换器，然后使用默认的 {@link AutoResponseBodyConverter} 来查找并应用。
     * <p>你也可以直接将自己的转换器写到 {@link Request#responseBodyConverter()} 上。
     *
     * @param responseBodyConverters 请求体转换器
     */
    public static void registerResponseBodyConverter(ResponseBodyConverter... responseBodyConverters) {
        ResponseBodyConverterCache.addUserConverters(responseBodyConverters);
    }

    private static class InvocationHandlerHolder {
        private static final AnnoHttpClientInvocationHandler INSTANCE = new AnnoHttpClientInvocationHandler();
    }
}
