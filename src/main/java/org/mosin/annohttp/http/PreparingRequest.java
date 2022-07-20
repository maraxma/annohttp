package org.mosin.annohttp.http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.exception.ConversionException;
import org.mosin.annohttp.http.exception.RequestFailedException;
import org.mosin.annohttp.http.exception.UnexpectedResponseException;

/**
 * 代表一个已经准备好的请求。
 *
 * @param <T> 期望返回的数据类型。
 * @author Mara.X.Ma
 * @since 2022-07-08
 */
public interface PreparingRequest<T> {

    /**
     * 自定义HttpClient相关的配置。需要用户自己提供一个HttpClientBuilder实例。在配置完成后将以用户提供的建造者生产新HttpClient发起请求。</p>
     * <p>为了性能，annohttp采用单例的HttpClient发起所有相关的请求，但此方法会打破规则使用用户定义好的HttpClientBuilder生成新的HttpClient发起请求。</p>
     * <p>推荐使用<code>HttpClientBuilderEnhancer.enhance(HttpClientBuilder.create())</code>来创建新的HttpClientBuilder。然后自定义配置并传入此方法。</p>
     * <p>并不推荐使用此方法，因为比较消耗性能。</p>
     *
     * @param httpClientBuilderSupplier HttpClient建造者提供器。
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customHttpClient(Supplier<HttpClientBuilder> httpClientBuilderSupplier);

    /**
     * 自定义请求头。
     *
     * @param headerConsumer 已经有的请求头列表，是可读可写的
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customRequestHeaders(Consumer<List<CoverableNameValuePair>> headerConsumer);

    /**
     * 自定义请求参数。
     *
     * @param queryConsumer 已经有的请求参数列表，是可读可写的
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customRequestQueries(Consumer<List<CoverableNameValuePair>> queryConsumer);

    /**
     * 自定义路径参数。
     *
     * @param pathVarConsumer 已经有的路径参数列表，是可读可写的
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customRequestPathVars(Consumer<Map<String, String>> pathVarConsumer);

    /**
     * 自定义表单参数。
     *
     * @param formFieldConsumer 已经有的表单参数列表，是可读可写的
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customRequestFormFields(Consumer<Map<String, Object>> formFieldConsumer);

    /**
     * 自定义请求地址。
     *
     * @param urlMapping URL映射函数，提供原有的URL，返回你需要定制的URL
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customRequestUrl(Function<String, String> urlMapping);

    /**
     * 自定义请求设置
     *
     * @param requestConfigBuilderConsumer 请求设置对象建造者消费器
     * @return {@link PreparingRequest} 本身
     */
    PreparingRequest<T> customRequestConfig(Consumer<RequestConfig.Builder> requestConfigBuilderConsumer);

    /**
     * 同步请求。
     * <p>此方法可能会抛出三种类型的异常，但它们都是运行时异常，用户可以选择性处理。</p>
     *
     * @return 响应体
     * @throws ConversionException         请求成功，但是在转换响应的时候出现异常（可能是用户的转换器中有异常，亦或是无法将响应转换为目标对象）
     * @throws RequestFailedException      请求失败时抛出，请求失败意味着服务端异常或者网络异常，当出现此异常时甚至连 {@link HttpResponse} 都未生成
     * @throws UnexpectedResponseException 当出现意料之外的响应时抛出此异常（一般来说是@Request.successCondition校验未通过）
     */
    T request() throws RequestFailedException, UnexpectedResponseException, ConversionException;

    /**
     * 异步请求。这是 {@link #request()} 方法的异步版本。
     *
     * @param executorService 线程池
     * @return 未来对象
     * @see #request()
     */
    CompletableFuture<T> requestAsync(Executor executorService);

    /**
     * 带回调的异步请求。这是 {@link #request()} 方法的异步版本。
     *
     * @param executorService 线程池
     * @param resultConsumer  结果消费器
     */
    void requestAsync(Executor executorService, Consumer<T> resultConsumer);

    /**
     * 同步请求返回经典的 {@link HttpResponse} 实例。用户需要自行处理响应<b>并在使用完毕后关闭相关的资源。</b>
     * <p><b>需要特别注意的是， {@link Request#successCondition()} 不对此方法的请求过程生效。</b>
     *
     * @return {@link HttpResponse} 实例
     */
    HttpResponse requestClassically() throws RequestFailedException;

    /**
     * {@link #requestClassically()} 的异步版本。
     *
     * @param executorService 线程池
     * @return 未来对象
     */
    CompletableFuture<HttpResponse> requestAsyncClassically(Executor executorService);

    /**
     * {@link #requestClassically()} 的带回调异步版本。
     *
     * @param executorService 线程池
     * @param resultConsumer  回调函数
     */
    void requestClassicallyAsync(Executor executorService, Consumer<HttpResponse> resultConsumer);

    /**
     * 发起同步请求，在请求成功后获得一个可直接操作返回体的 {@link OperableHttpResponse} 对象。
     *
     * @return {@link OperableHttpResponse} 对象
     */
    OperableHttpResponse requestOperable();

    /**
     * {@link #requestOperable()} 的异步版本。
     *
     * @param executorService 线程池
     * @return 未来对象
     */
    CompletableFuture<OperableHttpResponse> requestOperableAsync(Executor executorService);

    /**
     * {@link #requestOperable()} 的异步版本。
     *
     * @param executorService 线程池
     * @param resultConsumer  回调函数
     */
    void requestOperableAsync(Executor executorService, Consumer<OperableHttpResponse> resultConsumer);
}
