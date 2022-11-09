package org.mosin.annohttp.http.response.converter;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.Converter;

import java.nio.charset.Charset;

/**
 * 响应转换器。负责将响应转换为期望的实例。
 * <p>适用于 {@link Request#responseConverter()} ，如果直接在上面指定除 {@link AutoResponseConverter} 以外的其他 {@link ResponseConverter}，
 * 那么将不会调用 {@link ResponseConverter#canConvert(HttpResponse, AnnoHttpClientMetadata, ContentType, Charset)}  而是直接开始转换。
 *
 * @author Mara.X.Ma
 * @see Request#responseConverter()
 * @since 1.0.0 2022-07-19
 */
public interface ResponseConverter extends Converter {

    /**
     * 判定该转换器是否能应用于转换。
     *
     * @param httpResponse                响应体，不会是null
     * @param metadata                    一些可供使用的元数据，不会是null
     * @param computedResponseContentType 已计算的响应体类型，不会是null
     * @param computedResponseCharset     已计算的响应体字符编码，不会是null
     * @return 如果能，则返回true，否则返回false
     */
    boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset);

    /**
     * 开始转换。
     *
     * @param httpResponse                HTTP响应，不会是null，因为到这里的一定是成功请求的
     * @param metadata                    一些可供使用的元数据
     * @param computedResponseContentType 已计算的响应体类型，不会是null
     * @param computedResponseCharset     已计算的响应体字符编码，不会是null
     * @return 转换结果
     */
    Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata, ContentType computedResponseContentType, Charset computedResponseCharset);

}
