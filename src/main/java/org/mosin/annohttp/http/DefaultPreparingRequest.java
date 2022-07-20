package org.mosin.annohttp.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.exception.NoApplicableResponseBodyConverterException;
import org.mosin.annohttp.http.exception.RequestFailedException;
import org.mosin.annohttp.http.exception.UnexpectedResponseException;
import org.mosin.annohttp.http.method.HttpDeleteWithPayload;
import org.mosin.annohttp.http.method.HttpGetWithPayload;
import org.mosin.annohttp.http.method.HttpHeadWithPayload;
import org.mosin.annohttp.http.method.HttpOptionsWithPayload;
import org.mosin.annohttp.http.method.HttpTraceWithPayload;
import org.mosin.annohttp.http.proxy.HttpClientProxyContext;
import org.mosin.annohttp.http.proxy.RequestProxy;
import org.mosin.annohttp.http.request.converter.MapRequestBodyConverter;
import org.mosin.annohttp.http.request.converter.RequestBodyConverterCache;
import org.mosin.annohttp.http.response.converter.AutoResponseBodyConverter;
import org.mosin.annohttp.http.response.converter.ResponseBodyConverter;
import org.mosin.annohttp.http.response.converter.ResponseBodyConverterCache;
import org.mosin.annohttp.http.spel.SpelUtils;
import org.mosin.annohttp.http.visitor.ResponseVisitor;
import org.mosin.annohttp.http.visitor.ResponseVisitorCache;
import org.springframework.expression.EvaluationContext;

class DefaultPreparingRequest<T> implements PreparingRequest<T> {

    protected final HttpMethod requestType;

    protected String url;

    protected final HttpEntity httpEntity;
    protected final RequestProxy requestProxy;

    protected final List<CoverableNameValuePair> headers;
    protected final List<CoverableNameValuePair> queries;
    protected final Map<String, Object> formFields;
    protected final Map<String, String> pathVars;
    protected final AnnoHttpClientMetadata metadata;

    protected HttpClientBuilder userHttpClientBuilder;
    protected RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

    protected volatile CloseableHttpClient httpClient;
    protected static final Object HTTP_CLIENT_LOCK = new Object();

    DefaultPreparingRequest(HttpMethod requestType,
                            String url, 
                            Map<String, String> pathVars,
                            HttpEntity httpEntity,
                            RequestProxy requestProxy,
                            List<CoverableNameValuePair> headers,
                            List<CoverableNameValuePair> queries,
                            Map<String, Object> formFields,
                            AnnoHttpClientMetadata metadata) {
        this.requestType = requestType;
        this.url = url;
        this.pathVars = pathVars == null ? null : new LinkedHashMap<>(pathVars);
        this.httpEntity = httpEntity;
        this.requestProxy = requestProxy;
        this.headers = headers == null ? null : new ArrayList<>(headers);
        this.queries = queries == null ? null : new ArrayList<>(queries);
        this.formFields = formFields == null ? null : new LinkedHashMap<>(formFields);
        this.metadata = metadata;
    }

    @Override
    public PreparingRequest<T> customHttpClient(Supplier<HttpClientBuilder> httpClientBuilderSupplier) {
        if (httpClientBuilderSupplier != null) {
            HttpClientBuilder userHttpClientBuilder = httpClientBuilderSupplier.get();
            if (userHttpClientBuilder == null) {
                throw new IllegalArgumentException("httpClientBuilderSupplier must return an non-null HttpClientBuilder instance");
            }
            this.userHttpClientBuilder = userHttpClientBuilder;
        }
        return this;
    }

    @Override
    public PreparingRequest<T> customRequestHeaders(Consumer<List<CoverableNameValuePair>> headerConsumer) {
        if (headerConsumer != null) {
            headerConsumer.accept(headers);
        }
        return this;
    }

    @Override
    public PreparingRequest<T> customRequestQueries(Consumer<List<CoverableNameValuePair>> queryConsumer) {
        if (queryConsumer != null) {
            queryConsumer.accept(queries);
        }
        return this;
    }

    @Override
    public PreparingRequest<T> customRequestPathVars(Consumer<Map<String, String>> pathVarConsumer) {
        if (pathVarConsumer != null) {
            pathVarConsumer.accept(pathVars);
        }
        return this;
    }

    @Override
    public PreparingRequest<T> customRequestFormFields(Consumer<Map<String, Object>> formFieldConsumer) {
        if (formFieldConsumer != null) {
            formFieldConsumer.accept(formFields);
        }
        return this;
    }

    @Override
    public PreparingRequest<T> customRequestConfig(Consumer<RequestConfig.Builder> requestConfigBuilderConsumer) {
        if (requestConfigBuilderConsumer != null) {
        	requestConfigBuilderConsumer.accept(requestConfigBuilder);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T request() {
    	HttpResponse httpResponse = null;
    	try {
    		httpResponse = executeRequest();
    	} catch (Exception e) {
    		throw new RequestFailedException("", e);
		}

		// 从这里开始便有了 httpResponse，出现任何异常应当释放 HttpResponse 面的 Entity 所占用的资源
		// 如果用户需要自行处理
		try {
			Charset computedResponseCharset;
			ContentType computedResponseContentType;

			Request requestAnnotation = metadata.getRequestAnnotation();
			String userResponseContentType = requestAnnotation.responseContentType();
			boolean preferResponseContentType = requestAnnotation.preferUsingResponseContentType();

			ContentType responseContentType = ContentType.get(httpResponse.getEntity());

			String userResponseCharset = requestAnnotation.responseCharset();
			Charset responseCharset = responseContentType == null ? null : responseContentType.getCharset();
			boolean preferResponseCharset = requestAnnotation.preferUsingResponseCharset();

			if (preferResponseContentType && responseContentType != null) {
				computedResponseContentType = responseContentType;
			} else {
				computedResponseContentType = ContentType.parse(userResponseContentType);
			}

			if (preferResponseCharset && responseCharset != null) {
				// 如果优先使用响应头中的charset，那么直接查找使用，如果未查找到，那么使用用户定义的charset
				computedResponseCharset = responseCharset;
			} else {
				// 否则直接使用用户定义的charset
				computedResponseCharset = Charset.forName(userResponseCharset);
			}

			Class<? extends ResponseBodyConverter> converterClass = metadata.getRequestAnnotation().responseBodyConverter();
			if (converterClass == AutoResponseBodyConverter.class) {
				return (T) ResponseBodyConverterCache.AUTO_RESPONSE_BODY_CONVERTER.convert(httpResponse, metadata, computedResponseContentType, computedResponseCharset);
			} else {
				ResponseBodyConverter responseConverter = ResponseBodyConverterCache.getAll().get(converterClass);
				if (responseConverter == null) {
					throw new NoApplicableResponseBodyConverterException("Cannot find response convert '" + converterClass + "', please register it before using");
				}
				return (T) responseConverter.convert(httpResponse, metadata, computedResponseContentType, computedResponseCharset);
			}
		} finally {
			EntityUtils.consumeQuietly(httpResponse.getEntity());
		}
    }

    @Override
    public CompletableFuture<T> requestAsync(Executor executorService) {
        if (executorService == null) {
            throw new IllegalArgumentException("executorService cannot be null");
        }
        return CompletableFuture.supplyAsync(this::request, executorService);
    }

    @Override
    public void requestAsync(Executor executorService, Consumer<T> resultConsumer) {
        requestAsync(executorService).thenAccept(resultConsumer);
    }

    public CloseableHttpClient getHttpClient() {
        if (userHttpClientBuilder != null) {
            return userHttpClientBuilder.build();
        }
        return HttpComponentHolder.getHttpClientInstance();
    }

	@Override
	public PreparingRequest<T> customRequestUrl(Function<String, String> urlMapping) {
		if (urlMapping != null) {
			String userUrl = urlMapping.apply(url);
			if (userUrl == null || "".equals(userUrl.trim())) {
				throw new IllegalArgumentException("Url cannot be null or empty");
			}
			url = userUrl;
		}
		return this;
	}

	@Override
	public HttpResponse requestClassically() throws RequestFailedException {
		return executeRequest();
	}

	@Override
	public CompletableFuture<HttpResponse> requestAsyncClassically(Executor executorService) {
		return CompletableFuture.supplyAsync(() -> requestClassically(), executorService);
	}

	@Override
	public void requestClassicallyAsync(Executor executorService, Consumer<HttpResponse> resultConsumer) {
		requestAsyncClassically(executorService).thenAccept(resultConsumer);
	}

	@Override
	public OperableHttpResponse requestOperable() {
		return new OperableHttpResponse(executeRequest());
	}

	@Override
	public CompletableFuture<OperableHttpResponse> requestOperableAsync(Executor executorService) {
		return CompletableFuture.supplyAsync(() -> requestOperable(), executorService);
	}

	@Override
	public void requestOperableAsync(Executor executorService,
			Consumer<OperableHttpResponse> resultConsumer) {
		requestOperableAsync(executorService).thenAccept(resultConsumer);
	}

	protected void buildHttpClient() {
	    if (httpClient == null) {
	        synchronized (HTTP_CLIENT_LOCK) {
	            if (httpClient == null) {
	                if (userHttpClientBuilder != null) {
	                    httpClient = userHttpClientBuilder.build();
	                } else {
	                    httpClient = HttpComponentHolder.getHttpClientInstance();
	                }
	            }
	        }
	    }
	}

	/**
	 * 构建实际的请求对象。
	 * @return
	 */
	protected HttpUriRequest generateHttpRequest() {
	
	    String computedUrl = url;
	
	    // 处理路径参数，必须完全匹配，要区分大小写
	    if (pathVars != null) {
	        for (Map.Entry<String, String> en : pathVars.entrySet()) {
	            computedUrl = computedUrl.replace("{" + en.getKey() + "}", en.getValue());
	        }
	    }
	
	    // 处理查询参数
	    URI computedUri;
	    if (queries != null) {
	        List<NameValuePair> params = queries
	                .stream()
	                .map(e -> new BasicNameValuePair(e.getName(), e.getValue()))
	                .collect(Collectors.toList());
	        try {
	            computedUri = new URIBuilder(computedUrl).addParameters(params).build();
	        } catch (URISyntaxException e) {
	            throw new IllegalArgumentException("Cannot attach query parameters: " + queries + " to url " + computedUrl, e);
	        }
	    } else {
	        computedUri = URI.create(computedUrl);
	    }
	
	    // 处理Method
	    HttpEntityEnclosingRequestBase httpUriRequest;
	    if (requestType == HttpMethod.GET) {
	        httpUriRequest = new HttpGetWithPayload(computedUri);
	    } else if (requestType == HttpMethod.POST) {
	        httpUriRequest = new HttpPost(computedUri);
	    } else if (requestType == HttpMethod.DELETE) {
	        httpUriRequest = new HttpDeleteWithPayload(computedUri);
	    } else if (requestType == HttpMethod.OPTIONS) {
	        httpUriRequest = new HttpOptionsWithPayload(computedUri);
	    } else if (requestType == HttpMethod.PUT) {
	        httpUriRequest = new HttpPut(computedUri);
	    } else if (requestType == HttpMethod.TRACE) {
	        httpUriRequest = new HttpTraceWithPayload(computedUri);
	    } else if (requestType == HttpMethod.HEAD) {
	        httpUriRequest = new HttpHeadWithPayload(computedUri);
	    } else if (requestType == HttpMethod.PATCH) {
	        httpUriRequest = new HttpPatch(computedUri);
	    } else {
	        throw new IllegalArgumentException("Unsupported request type: " + requestType);
	    }
	
	    // 处理请求头
	    if (headers != null) {
	        Header[] finalHeaders = headers
	                .stream()
	                .map(e -> new BasicHeader(e.getName(), e.getValue())).toList()
	                .toArray(new Header[0]);
	        httpUriRequest.setHeaders(finalHeaders);
	    }
	    
	    // 处理Body 和 表单
	    ContentType contentType = null;
	    for (CoverableNameValuePair coverableNameValuePair : headers) {
			if (coverableNameValuePair.getName().equalsIgnoreCase(HTTP.CONTENT_TYPE)) {
				contentType = ContentType.parse(coverableNameValuePair.getValue());
				break;
			}
		}
	    if (contentType == null) {
	    	// 如果用户未设定Content-Type，那么自动设定
	    	// 如果用户设定了，那么就要校验，在有Form的情况下，Content-Type只能设定为urlencoded或multipart-form-data
	    	boolean otherTypeFound = formFields.values().stream().anyMatch(e -> !(e instanceof String));
	    	if (otherTypeFound) {
	    		contentType = ContentType.MULTIPART_FORM_DATA;
	    		headers.add(new CoverableNameValuePair(HTTP.CONTENT_TYPE, ContentType.MULTIPART_FORM_DATA.toString()));
	    	} else {
	    		contentType = ContentType.APPLICATION_FORM_URLENCODED;
	    		headers.add(new CoverableNameValuePair(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString()));
	    	}
	    } else {
	    	if (!ContentType.APPLICATION_FORM_URLENCODED.getMimeType().equalsIgnoreCase(contentType.getMimeType())
		    		|| !ContentType.MULTIPART_FORM_DATA.getMimeType().equalsIgnoreCase(contentType.getMimeType())) {
	    		throw new IllegalArgumentException("If you are using @FormField or @FormFields, the content-type must be set to urlencoded or multipart-data. Or you can ignore content-type, annohttp will set it automatically");
	    	}
	    }
	    if (httpEntity != null) {
	        httpUriRequest.setEntity(httpEntity);
	    } else {
	    	if (formFields != null) {
	    		RequestBodyConverterCache.getAll().get(MapRequestBodyConverter.class).convert(httpUriRequest, contentType, metadata, computedUrl);
	    	}
	    }
	    
	    // 处理超时设置
	    int socketTimeout = metadata.getRequestAnnotation().socketTimeoutInSeconds();
	    if (socketTimeout != -1) {
	    	requestConfigBuilder.setSocketTimeout(socketTimeout);
	    }
	    int connectionTimeout = metadata.getRequestAnnotation().connectionTimeoutInSeconds();
	    if (connectionTimeout != -1) {
	    	requestConfigBuilder.setConnectTimeout(connectionTimeout);
	    }
	    
	    // 处理disableRedirects
	    if (metadata.getRequestAnnotation().disableRedirects()) {
	    	requestConfigBuilder.setRedirectsEnabled(false);
	    }
        
        // 处理其他可能出现的设定参数
        processAdditionalParameters(metadata, httpUriRequest, requestConfigBuilder);
	
	    // 附加请求
	    // 附加配置
	    httpUriRequest.setConfig(requestConfigBuilder.build());
	
	    return httpUriRequest;
	}
	
	protected void processAdditionalParameters(AnnoHttpClientMetadata metadata2,
			HttpEntityEnclosingRequestBase httpUriRequest, Builder requestConfigBuilder2) {
		
	}

	/**
	 * 执行请求并获得返回。
	 * @return {@link HttpResponse} 实例
	 */
	protected HttpResponse executeRequest() {
		HttpUriRequest httpUriRequest = generateHttpRequest();
        buildHttpClient();
        HttpResponse httpResponse = null;
		Exception requestException = null;
        try {
            if (requestProxy != null) {
                httpResponse = httpClient.execute(httpUriRequest, new HttpClientProxyContext(requestProxy));
            } else {
                httpResponse = httpClient.execute(httpUriRequest);
            }
        } catch (IOException e) {
            requestException = e;
        }

		// 处理visitor
		Class<? extends ResponseVisitor> responseVisitorClass = metadata.getRequestAnnotation().responseVisitor();
		ResponseVisitor responseVisitor = ResponseVisitorCache.getOrCreate(responseVisitorClass);
		try {
			responseVisitor.visit(userHttpClientBuilder == null ? HttpComponentHolder.getHttpClientBuilderInstance() : userHttpClientBuilder, httpClient, this, httpResponse, requestException);
		} catch (Throwable e) {
			throw new RuntimeException("Something wrong with ResponseVisitor '" + responseVisitor + "'", e);
		}

		// 处理successCondition
		String successCondition = metadata.getRequestAnnotation().successCondition();
		EvaluationContext evaluationContext = SpelUtils.prepareSpelContext(metadata.getRequestMethodArguments());
		evaluationContext.setVariable("httpResponse", httpResponse);
		Object res = SpelUtils.executeSpel(successCondition, evaluationContext, Object.class);
		if (res instanceof Boolean b) {
			if (!b) {
				throw new UnexpectedResponseException("Unexpected response, the spel successCondition returns false: " + successCondition);
			}
		} else {
			throw new IllegalArgumentException("@Request.successCondition() must return a boolean value");
		}

        return httpResponse;
	} 
}