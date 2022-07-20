package org.mosin.annohttp.http;

/**
 * 代表一个AnnoHttp客户端的上下文保持器。
 */
public class AnnoHttpClientContextHolder {

    static final InheritableThreadLocal<AnnoHttpClientMetadata> INFO = new InheritableThreadLocal<AnnoHttpClientMetadata>();

    public static AnnoHttpClientMetadata getMetadata() {
        return INFO.get();
    }

    static void setMetadata(AnnoHttpClientMetadata annoHttpClientMetadata) {
        INFO.set(annoHttpClientMetadata);
    }
}
