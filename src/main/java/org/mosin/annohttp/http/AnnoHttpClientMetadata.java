package org.mosin.annohttp.http;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.mosin.annohttp.annotation.Request;

public interface AnnoHttpClientMetadata {

	/**
	 * 获得发起请求的客户端类，一般是一个标注了@Request的接口。
	 * @return 客户端类
	 */
    Class<?> getServiceClientClass();

    /**
     * 获得发起请求的客户端实例，一般是一个标注了@Request的接口的实例。
     * @return 客户端实例
     */
    Object getServiceClient();

    /**
     * 获得发起请求的方法的返回类型
     * @return 返回类型
     */
    Class<?> getRequestMethodReturnClass();
    
    /**
     * 获得请求方法返回类型的实际类型。如果返回类是 {@link PreparingRequest} ，那么这个方法返回 {@link PreparingRequest} 
     * 所包裹的实际类型。
     * @return 返回的实际类型
     */
    Type getRequestMethodActualType();

    /**
     * 获得标注在客户端接口上的 {@link Request} 实例。
     * @return {@link Request} 实例。
     */
    Request getRequestAnnotation();

    /**
     * 获得客户端的请求对应的方法。
     * @return {@link Method} 实例。
     */
    Method getRequestMethod();
    
    /**
     * 获得请求方法上附带的参数
     * @return
     */
    Object[] getRequestMethodArguments();
}
