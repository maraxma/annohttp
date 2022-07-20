package org.mosin.annohttp.http;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.mosin.annohttp.annotation.Request;

public class DefaultAnnoHttpClientMetadata implements AnnoHttpClientMetadata {

    Class<?> serviceClientClass;
    Object serviceClient;
    Class<?> requestMethodReturnClass;
    Request requestAnnotation;
    Method requestMethod;
    Object[] requestArguments;
    Type requestMethodRerturnActualType;

    @Override
    public Class<?> getServiceClientClass() {
        return serviceClientClass;
    }

    @Override
    public Object getServiceClient() {
        return serviceClient;
    }

    @Override
    public Request getRequestAnnotation() {
        return requestAnnotation;
    }

    @Override
    public Method getRequestMethod() {
        return requestMethod;
    }

    @Override
    public Object[] getRequestMethodArguments() {
        return requestArguments;
    }

    @Override
    public Class<?> getRequestMethodReturnClass() {
        return requestMethodReturnClass;
    }

    @Override
    public Type getRequestMethodActualType() {
        return requestMethodRerturnActualType;
    }
}
