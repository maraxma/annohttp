package org.mosin.annohttp.http.protocol;

import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.PreparingRequest;

public interface ProtocolHandler {

    String PROTOCOL_SPLITTER = "/";

    /**
     * 返回此处理器所支持的协议名称
     * @return 协议名称
     */
    String protocol();

    /**
     * 开始处理。
     * @param metadata 一些可以用到的元数据
     * @param preparingRequest 已经生成好的 {@link PreparingRequest} 对象，在其上面可以进行很多自定义的操作
     */
    void handle(AnnoHttpClientMetadata metadata, PreparingRequest<?> preparingRequest);
}
