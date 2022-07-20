package org.mosin.annohttp.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.mosin.annohttp.annotation.Body;
import org.mosin.annohttp.annotation.FormField;
import org.mosin.annohttp.annotation.FormFields;
import org.mosin.annohttp.annotation.Headers;
import org.mosin.annohttp.annotation.PathVar;
import org.mosin.annohttp.annotation.PathVars;
import org.mosin.annohttp.annotation.Queries;
import org.mosin.annohttp.annotation.Query;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.proxy.RequestProxy;
import org.mosin.annohttp.http.request.converter.AutoRequestBodyConverter;
import org.mosin.annohttp.http.request.converter.RequestBodyConverterCache;
import org.mosin.annohttp.http.spel.SpelUtils;
import org.springframework.expression.EvaluationContext;

public class AnnoHttpClientInvocationHandler implements InvocationHandler {

    protected static final String MAP_KEY_NAME = "name";
    protected static final String MAP_KEY_VALUE = "value";
    protected static final String MAP_KEY_COVERABLE = "coverable";

    public static final String DEFAULT_BYTES_FIELD_NAME = "Bytes";
    public static final String DEFAULT_STRING_FIELD_NAME = "String";
    public static final String DEFAULT_OBJECT_FIELD_NAME = "Object";

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 生成PreparingRequest实例，视情况使用其发起请求或者直接返回它
        // 需要构造PreparingRequest实例，延迟请求
        Type genericType = method.getGenericReturnType();
        Class<?> returnType = method.getReturnType();
        PreparingRequest<?> preparingRequest;
        DefaultAnnoHttpClientMetadata metadata = new DefaultAnnoHttpClientMetadata();
        Request requestAnno = method.getAnnotation(Request.class);
        Parameter[] parameters = method.getParameters();
        EvaluationContext evaluationContext = SpelUtils.prepareSpelContext(args);

        /*       1 处理HttpMethod  */
        HttpMethod computedMethod = processMethod(requestAnno, parameters, args);

        /*       2 处理URL         */
        String computedUrl = processUrl(requestAnno, parameters, args, evaluationContext);

        /*       3 处理PathVars     */
        Map<String, String> pathVars = processPathVars(requestAnno, parameters, args);

        /*       4 处理请求头       */
        // 注意优先级，参数请求头 > @Request注解请求头 > @ContentType请求头
        LinkedList<CoverableNameValuePair> headers = processHeaders(requestAnno, method, parameters, args, evaluationContext);

        /*       5 处理请参数       */
        // 注意优先级，参数列表中的查询参数 > @Request注解查询参数
        List<CoverableNameValuePair> queries = processQueryParameters(computedUrl, parameters, args, requestAnno, evaluationContext);

        /*       6 处理代理设置     */
        RequestProxy requestProxy = processProxy(requestAnno, evaluationContext);

        /*       7 处理Body      */
        // Body可以是各种类型的，annohttp会根据不同的类型采取不同的策略
        // Body只能有一个，且和@Field或者@Fields冲突，因为都会占用Body
        HttpEntity httpEntity = processBody(headers, requestAnno, parameters, args, metadata, evaluationContext);

        /*    8 处理FormField */
        LinkedHashMap<String, Object> formFields = processFormFields(parameters, args);

        /*    9 处理successCondition */
        // 在PreparingRequest中处理

        /*   10 处理Visitor  */
        // 在PreparingRequest中处理

        // 视条件发起请求
        // 如果返回值非PreparingRequest，那么直接请求并转换结果
        // 如果是，那么包装之，然后返回
        // 组装AnnotationHttpClientMetadata
        DefaultAnnoHttpClientMetadata annoHttpClientMetadata = new DefaultAnnoHttpClientMetadata();
        annoHttpClientMetadata.serviceClient = this;
        annoHttpClientMetadata.requestMethodReturnClass = returnType;
        annoHttpClientMetadata.serviceClientClass = this.getClass();
        annoHttpClientMetadata.requestMethod = method;
        annoHttpClientMetadata.requestArguments = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
        annoHttpClientMetadata.requestAnnotation = requestAnno;
        preparingRequest = new DefaultPreparingRequest<>(
                computedMethod,
                computedUrl,
                pathVars,
                httpEntity,
                requestProxy,
                headers,
                queries,
                formFields,
                annoHttpClientMetadata);
        if (PreparingRequest.class.isAssignableFrom(returnType)) {
            // 需要构造PreparingRequest实例，延迟请求
            if (!(genericType instanceof ParameterizedType)) {
                // should never happen
                throw new IllegalStateException("PreparingRequest is not a ParameterizedType(SHOULD NEVER HAPPEN)");
            }
            annoHttpClientMetadata.requestMethodRerturnActualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            return preparingRequest;
        } else {
            annoHttpClientMetadata.requestMethodRerturnActualType = genericType;
            return preparingRequest.request();
        }
    }

    private boolean findAnnotation(Parameter[] parameters, Class<? extends Annotation> annoClass) {
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(annoClass)) {
                return true;
            }
        }
        return false;
    }

    private void checkHeader(String headerName, Object headerValue) {
        if (null == headerName || "".equals(headerName) || headerValue == null) {
            throw new IllegalArgumentException("Header name cannot null or empty; headerValue cannot be null");
        }
    }

    private CoverableNameValuePair getQueryParameterFromStringStyled(String stringStyledQuery, boolean queryCoverable) {
        if (null == stringStyledQuery || "".equals(stringStyledQuery.trim()) || !stringStyledQuery.contains("=")) {
            throw new IllegalArgumentException("Query definition string is invalid: " + stringStyledQuery);
        }
        int i = stringStyledQuery.indexOf("=");
        String queryName = stringStyledQuery.substring(0, i).trim();
        String queryValue = stringStyledQuery.substring(i + 1).trim();
        if ("".equals(queryName)) {
            throw new IllegalArgumentException("Query name '" + queryName + "' is invalid in query string '" + stringStyledQuery + "'");
        }
        return new CoverableNameValuePair(queryName, queryValue, queryCoverable);
    }

    private CoverableNameValuePair getHeaderFromStringStyled(String stringStyledHeader, boolean coverable) {
        if (null == stringStyledHeader || "".equals(stringStyledHeader) || !stringStyledHeader.contains(":")) {
            throw new IllegalArgumentException("header string is invalid: " + stringStyledHeader);
        }
        int i = stringStyledHeader.indexOf(":");
        String headerName = stringStyledHeader.substring(0, i).trim();
        String headerValue = stringStyledHeader.substring(i + 1).trim();
        if ("".equals(headerName)) {
            throw new IllegalArgumentException("Header name '" + headerName + "' is invalid in header string '" + stringStyledHeader + "'");
        }
        return new CoverableNameValuePair(headerName, headerValue, coverable);
    }

    private NameValuePair getPathVarFromStringStyled(String pathVar) {
        if (null == pathVar || "".equals(pathVar) || !pathVar.contains("=")) {
            throw new IllegalArgumentException("PathVar string is invalid: " + pathVar);
        }
        int i = pathVar.indexOf(":");
        String varName = pathVar.substring(0, i).trim();
        String varValue = pathVar.substring(i + 1).trim();
        if ("".equals(varName)) {
            throw new IllegalArgumentException("PathVar name '" + varName + "' is invalid in pathVar string '" + pathVar + "'");
        }
        if ("".equals(varValue)) {
            throw new IllegalArgumentException("PathVar value '" + varValue + "' is invalid in pathVar string '" + pathVar + "'");
        }
        return new BasicNameValuePair(varName, varValue);
    }

    private void addCoverable(LinkedList<CoverableNameValuePair> existing, CoverableNameValuePair incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("Incoming coverable header/query paratmeter cannot be null");
        }
        Iterator<CoverableNameValuePair> iter = existing.iterator();
        while (iter.hasNext()) {
            CoverableNameValuePair existingCoverable = iter.next();
            NameValuePair existingNameValuePair = (NameValuePair) existingCoverable;
            NameValuePair incomingNameValuePair = (NameValuePair) incoming;
            if (existingNameValuePair.getName().equalsIgnoreCase(incomingNameValuePair.getName())) {
                if (existingCoverable.isCoverable()) {
                    iter.remove();
                }
            }
        }
        existing.addLast(incoming);
    }

    protected String processUrl(Request requestAnno, Parameter[] parameters, Object[] args, EvaluationContext evaluationContext) {
        String computedUrl = requestAnno.url();
        String urlSpel = requestAnno.urlSpel();
        if (!"".equals(computedUrl) && !"".equals(urlSpel)) {
            throw new IllegalArgumentException("Only can set one of @Request.url and @Request.urlSpel");
        }
        if (!"".equals(urlSpel)) {
            Object res = SpelUtils.executeSpel(urlSpel, evaluationContext, Object.class);
            if (!(res instanceof String)) {
                throw new IllegalArgumentException("@Request.urlSpel must return a string instance");
            }
            computedUrl = (String) res;
        }
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            if (parameter.isAnnotationPresent(org.mosin.annohttp.annotation.Url.class)) {
                if (!String.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@Url accept String class only");
                }
                computedUrl = (String) args[i];
                break;
            }
        }
        return computedUrl;
    }

    protected HttpMethod processMethod(Request requestAnno, Parameter[] parameters, Object[] args) {
        HttpMethod computedMethod = requestAnno.method();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            if (parameter.isAnnotationPresent(org.mosin.annohttp.annotation.Method.class)) {
                if (!HttpMethod.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@Method accept HttpMethod class only");
                }
                computedMethod = (HttpMethod) args[i];
                break;
            }
        }
        return computedMethod;
    }

    protected Map<String, String> processPathVars(Request requestAnno, Parameter[] parameters, Object[] args) {
        Map<String, String> pathVars = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            if (parameter.isAnnotationPresent(org.mosin.annohttp.annotation.PathVar.class)) {
                if (parameter.isAnnotationPresent(PathVars.class)) {
                    throw new IllegalArgumentException("A parameter can put one of PathVar/PathVars");
                }
                if (!String.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@PathVar accept String class only");
                }
                NameValuePair nameValuePair = getPathVarFromStringStyled((String) args[i]);
                pathVars.put(nameValuePair.getName(), nameValuePair.getValue());
            } else if (parameter.isAnnotationPresent(PathVars.class)) {
                if (parameter.isAnnotationPresent(PathVar.class)) {
                    throw new IllegalArgumentException("A parameter can put one of PathVar/PathVars");
                }
                if (!Map.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@PathVar accept Map class only(Map<String, String>)");
                }
                @SuppressWarnings("unchecked")
                Map<String, String> pathVarsMap = (Map<String, String>) args[i];
                for (Map.Entry<String, String> entry : pathVarsMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    key = key == null ? null : key.trim();
                    value = value == null ? null : value.trim();
                    if (key == null || "".equals(key)) {
                        throw new IllegalArgumentException("pathVar's name caonnt be null or empty");
                    }
                    if (value == null || "".equals(value)) {
                        throw new IllegalArgumentException("pathVar's value caonnt be null or empty");
                    }
                    pathVarsMap.put(key, value);
                }
            }
        }

        return pathVars;
    }

    protected LinkedList<CoverableNameValuePair> processHeaders(Request requestAnno, Method method, Parameter[] parameters, Object[] args, EvaluationContext evaluationContext) {
        LinkedList<CoverableNameValuePair> headers = new LinkedList<>();
        boolean headerCoverable = requestAnno.headerCoverable();
        // 4.1 处理参数请求头
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            if (parameter.isAnnotationPresent(org.mosin.annohttp.annotation.Header.class)) {
                if (!String.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@Header parameter should be type of String");
                }
                String argHeaderValue = (String) args[i];
                if (null == argHeaderValue || "".equals(argHeaderValue)) {
                    throw new IllegalArgumentException("arg for @Header cannot be null or empty: " + parameter.getName() + "(" + i + ")");
                }
                org.mosin.annohttp.annotation.Header headerAnno = parameter.getAnnotation(org.mosin.annohttp.annotation.Header.class);
                String annoHeaderName = headerAnno.value();
                if (annoHeaderName == null || "".equals(annoHeaderName = annoHeaderName.trim())) {
                    throw new IllegalArgumentException("Header name cannot be null or empty: " + parameter.getName());
                }
                addCoverable(headers, new CoverableNameValuePair(annoHeaderName, argHeaderValue));
            }
            if (parameter.isAnnotationPresent(Headers.class)) {
                if (!Map.class.isAssignableFrom(parameterType) || !String[].class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@Headers parameter should be type of String[] or Map<String, String>");
                }
                if (String[].class.isAssignableFrom(parameterType)) {
                    String[] headerArray = (String[]) args[i];
                    for (String s : headerArray) {
                        addCoverable(headers, getHeaderFromStringStyled(s, headerCoverable));
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, String> headerMap = (Map<String, String>) args[i];
                    headerMap.forEach((k, v) -> {
                        checkHeader(k, v);
                        addCoverable(headers, new CoverableNameValuePair(k, v, headerCoverable));
                    });
                }
            }
        }
        // 4.2 处理@Request中用户定义的请求头
        String[] annoHeaders = requestAnno.headers();
        String annoSpelHeaders = requestAnno.headersSpel();
        if (!"".equals(annoSpelHeaders) && annoHeaders.length != 0) {
            throw new IllegalArgumentException("You can only use one approach(headersSpel or headers) to set headers in @Request");
        } else {
            if (annoHeaders.length != 0) {
                for (String annoHeader : annoHeaders) {
                    CoverableNameValuePair h = getHeaderFromStringStyled(annoHeader, headerCoverable);
                    addCoverable(headers, h);
                }
            } else {
                if (!"".equals(annoSpelHeaders.trim())) {
                    Object spelRes = SpelUtils.executeSpel(annoSpelHeaders, evaluationContext, Object.class);
                    if (spelRes instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = ((Map<String, Object>) spelRes);
                        m.forEach((k, v) -> {
                            checkHeader(k, v);
                            addCoverable(headers, new CoverableNameValuePair(k, String.valueOf(v), false));
                        });
                    } else if (spelRes instanceof Object[] array) {
                        for (Object o : array) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> m = ((Map<String, Object>) o);
                            String headerName = String.valueOf(m.get(MAP_KEY_NAME));
                            String headerValue = String.valueOf(m.get(MAP_KEY_VALUE));
                            boolean coverable = Boolean.parseBoolean(String.valueOf(m.get(MAP_KEY_COVERABLE)));
                            boolean headerInvalid = false;
                            if (null != headerName) {
                                headerName = headerName.trim();
                                if ("".equals(headerName)) {
                                    headerInvalid = true;
                                }
                            } else {
                                headerInvalid = true;
                            }
                            if (null != headerValue) {
                                headerValue = headerValue.trim();
                            } else {
                                headerInvalid = true;
                            }
                            if (headerInvalid) {
                                throw new IllegalArgumentException("Your header on @Request headerSpel is invalid, please check: " + m);
                            }
                            addCoverable(headers, new CoverableNameValuePair(headerName, headerValue, coverable));
                        }
                    }
                }
            }
        }
        // 4.3 处理独有的注解式请求头
        processContentTypeAnnotation(headers, method, requestAnno);
        // 4.4 处理@Request.contentType()
        String annoContentType = requestAnno.contentType();
        if (!"".equals(annoContentType.trim())) {
            headers.add(new CoverableNameValuePair(HTTP.CONTENT_TYPE, annoContentType, headerCoverable));
        }

        return headers;
    }

    protected HttpEntity processBody(List<CoverableNameValuePair> existingHeaders, Request requestAnno, Parameter[] parameters, Object[] args, AnnoHttpClientMetadata metadata, EvaluationContext evaluationContext) {
        HttpEntity httpEntity;
        ContentType computedRequestContentType = null;
        for (CoverableNameValuePair header : existingHeaders) {
            if (header.getName().equalsIgnoreCase(HTTP.CONTENT_TYPE)) {
                computedRequestContentType = ContentType.parse(header.getValue());
                break;
            }
        }
        if (computedRequestContentType == null) {
            computedRequestContentType = ContentType.APPLICATION_JSON;
        }
        int bodyFound = 0;
        int bodyIndex = -1;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Body.class)) {
                bodyFound++;
                bodyIndex = i;
            }
            if (bodyFound > 1) {
                throw new IllegalArgumentException("You cannot use more than 1 @Body");
            }
        }
        Class<?> requestBodyConverterClass = requestAnno.requestBodyConverter();
        if (bodyIndex != -1) {
            // 在参数列表中找到唯一的@Body
            Body bodyAnno = parameters[bodyIndex].getAnnotation(Body.class);
            httpEntity = convertRequestBody(requestBodyConverterClass, args[bodyIndex], computedRequestContentType, metadata, bodyAnno.value());
        } else {
            // 在参数列表中找不到Body才处理注解上的Body
            // 不要忘了在@Request中也有Body的设定
            String annoBodyString = requestAnno.bodyString();
            byte[] annoBodyBytes = requestAnno.bodyBytes();
            String annoBodySpel = requestAnno.bodySpel();
            if (!"".equals(annoBodyString)) {
                if (!"".equals(annoBodySpel) || annoBodyBytes.length != 0) {
                    throw new IllegalArgumentException("You can only use one of bodyString/bodyBytes/bodySpel to set body on @Request");
                }
                httpEntity = convertRequestBody(requestBodyConverterClass, annoBodyString, computedRequestContentType, metadata, DEFAULT_STRING_FIELD_NAME);
            } else if (annoBodyBytes.length != 0) {
                if (!"".equals(annoBodySpel)) {
                    throw new IllegalArgumentException("You can only use one of bodyString/bodyBytes/bodySpel to set body on @Request");
                }
                httpEntity = convertRequestBody(requestBodyConverterClass, annoBodyBytes, computedRequestContentType, metadata, DEFAULT_BYTES_FIELD_NAME);
            } else {
                Object res = SpelUtils.executeSpel(annoBodySpel, evaluationContext, Object.class);
                httpEntity = convertRequestBody(requestBodyConverterClass, res, computedRequestContentType, metadata, DEFAULT_OBJECT_FIELD_NAME);
            }
        }

        return httpEntity;
    }

    protected HttpEntity convertRequestBody(Class<?> requestBodyConverterClass, Object source,
                                            ContentType computedRequestContentType, AnnoHttpClientMetadata metadata, String formFieldName) {
        if (requestBodyConverterClass == AutoRequestBodyConverter.class) {
            return RequestBodyConverterCache.AUTO_REQUEST_BODY_CONVERTER.convert(source, computedRequestContentType, metadata, formFieldName);
        } else {
            if (!RequestBodyConverterCache.getAll().containsKey(requestBodyConverterClass)) {
                throw new IllegalStateException("The RequestBodyConverter '" + requestBodyConverterClass.getName() + "' cannot be found, please register it before using");
            }
            return RequestBodyConverterCache.getAll().get(requestBodyConverterClass).convert(source, computedRequestContentType, metadata, formFieldName);
        }
    }

    protected void processContentTypeAnnotation(LinkedList<CoverableNameValuePair> existingHeades, Method method, Request requestAnno) {
        Annotation contentTypeAnno = null;
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.getClass().getSimpleName().startsWith("ContentType")) {
                contentTypeAnno = annotation;
                break;
            }
        }
        ContentType contentType;
        if (contentTypeAnno != null) {
            String contentTypeName = contentTypeAnno.getClass().getName();
            if (contentTypeName.contains("ApplicationJson")) {
                contentType = ContentType.APPLICATION_JSON;
            } else if (contentTypeName.contains("ApplicationXml")) {
                contentType = ContentType.APPLICATION_XML;
            } else if (contentTypeName.contains("ApplicationFormUrlEncoded")) {
                contentType = ContentType.APPLICATION_FORM_URLENCODED;
            } else if (contentTypeName.contains("PlainText")) {
                contentType = ContentType.TEXT_PLAIN;
            } else if (contentTypeName.contains("ApplicationMultipartFormData")) {
                contentType = ContentType.MULTIPART_FORM_DATA;
            } else if (contentTypeName.contains("Wildcard")) {
                contentType = ContentType.WILDCARD;
            } else {
                // should never happen
                throw new IllegalArgumentException("Unsupported annotation ContentType: " + contentTypeName);
            }

            // 注意优先级，注解指定ContentType的优先级是最低的
            addCoverable(existingHeades, new CoverableNameValuePair(HTTP.CONTENT_TYPE, contentType.toString(), requestAnno.headerCoverable()));
        }
    }

    protected LinkedHashMap<String, Object> processFormFields(Parameter[] parameters, Object[] args) {
        boolean bodyExisted = findAnnotation(parameters, Body.class);
        LinkedHashMap<String, Object> formFields = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(FormField.class)) {
                if (parameters[i].isAnnotationPresent(FormFields.class)) {
                    throw new IllegalArgumentException("Only can use one of @FormField/@FormFields for one parameter");
                }
                if (bodyExisted) {
                    throw new IllegalArgumentException("@Body is exist, cannot use @FormField/@FormFileds any more because they are occupy request body both");
                }
                FormField formFieldAnno = parameters[i].getAnnotation(FormField.class);
                String formFieldName = formFieldAnno.value();
                Object formFieldValue = args[i];
                checkFormField(formFieldName, formFieldValue);
                formFields.put(formFieldName, formFieldValue);
            } else if (parameters[i].isAnnotationPresent(FormFields.class)) {
                if (parameters[i].isAnnotationPresent(FormField.class)) {
                    throw new IllegalArgumentException("Only can use one of @FormField/@FormFields for one parameter");
                }
                if (bodyExisted) {
                    throw new IllegalArgumentException("@Body is exist, cannot use @FormField/@FormFileds any more because they are occupy request body both");
                }
                if (!(args[i] instanceof Map)) {
                    throw new IllegalArgumentException("@FormFields can accept Map only");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) args[i];
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String formFieldName = entry.getKey();
                    Object formFieldValue = entry.getValue();
                    checkFormField(formFieldName, formFieldValue);
                    formFields.put(formFieldName, formFieldValue);
                }
            }
        }

        return formFields;
    }

    protected LinkedList<CoverableNameValuePair> processQueryParameters(String url, Parameter[] parameters, Object[] args, Request requestAnno, EvaluationContext evaluationContext) {
        LinkedList<CoverableNameValuePair> queries = new LinkedList<>();
        boolean queryCoverable = requestAnno.queryCoverable();
        // 5.1 处理方法参数列表中给出的请求参数
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            if (parameter.isAnnotationPresent(Query.class)) {
                if (parameter.isAnnotationPresent(Queries.class)) {
                    throw new IllegalArgumentException("You can only use one of @Query & @Queries");
                }
                if (!String.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@Query support String type only");
                }
                Query queryAnno = parameter.getAnnotation(Query.class);
                String qKey = queryAnno.value();
                if (qKey == null || "".equals(qKey.trim())) {
                    throw new IllegalArgumentException("Query parameter's key cannot be null or empty");
                }
                addCoverable(queries, new CoverableNameValuePair(qKey, (String) args[i], queryCoverable));
            } else if (parameter.isAnnotationPresent(Queries.class)) {
                if (parameter.isAnnotationPresent(Query.class)) {
                    throw new IllegalArgumentException("You can only use one of @Query & @Queries");
                }
                if (!Map.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("@Queries support Map<String, String> type only");
                }
                @SuppressWarnings("unchecked")
                Map<String, String> queryMap = (Map<String, String>) args[i];
                for (Map.Entry<String, String> en : queryMap.entrySet()) {
                    String queryName = en.getKey();
                    String queryValue = en.getValue();
                    checkQueryParameter(queryName, queryValue);
                    addCoverable(queries, new CoverableNameValuePair(queryName, queryValue, queryCoverable));
                }
            }
        }
        // 5.2 处理@Request.queries
        String[] annoQueries = requestAnno.queries();
        for (String q : annoQueries) {
            addCoverable(queries, getQueryParameterFromStringStyled(q, queryCoverable));
        }
        // 5.3 处理@Request.queriesSpel
        String queriesSpel = requestAnno.queriesSpel();
        if (!"".equals(queriesSpel.trim())) {
            Object res = SpelUtils.executeSpel(queriesSpel, evaluationContext, Object.class);
            if (res instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> m = (Map<String, String>) res;
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    String queryName = entry.getKey();
                    String queryValue = entry.getValue();
                    checkQueryParameter(queryName, queryValue);
                    addCoverable(queries, new CoverableNameValuePair(queryName, queryValue));
                }
            } else if (res instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> l = (List<Map<String, Object>>) res;
                for (Map<String, Object> map : l) {
                    String queryName = (String) map.get("name");
                    String queryValue = (String) map.get("value");
                    checkQueryParameter(queryName, queryValue);
                    boolean coverable = Boolean.parseBoolean((String) map.get("coverable"));
                    addCoverable(queries, new CoverableNameValuePair(queryName, queryValue, coverable));
                }
            }
        }

        return queries;
    }

    private void checkQueryParameter(String queryName, String queryValue) {
        if (queryName == null || "".equals(queryName.trim())) {
            throw new IllegalArgumentException("Query parameter's key cannot be null or empty");
        }
        if (queryValue == null) {
            throw new IllegalArgumentException("Query parameter's value cannot be null");
        }
    }

    private void checkFormField(String fieldName, Object fieldValue) {
        if ("".equals(fieldName.trim())) {
            throw new IllegalArgumentException("Form field name cannot be empty");
        }
        if (fieldValue == null) {
            throw new IllegalArgumentException("Form field value cannot be null");
        }
    }

    protected RequestProxy processProxy(Request requestAnno, EvaluationContext evaluationContext) {
        RequestProxy requestProxy = null;
        String requestProxySpel = requestAnno.proxy();
        if (!"".equals(requestProxySpel)) {
            Object requestProxySpelResult = SpelUtils.executeSpel(requestProxySpel, evaluationContext, Object.class);
            if (!(requestProxySpelResult instanceof RequestProxy) || requestProxySpelResult == null) {
                throw new IllegalArgumentException("proxy must return an instance of RequestProxy");
            } else {
                requestProxy = (RequestProxy) requestProxySpelResult;
            }
        }
        return requestProxy;
    }
}
