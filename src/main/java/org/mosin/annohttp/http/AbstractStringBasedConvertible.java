package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;
import org.mosin.annohttp.http.exception.ConversionException;

/**
 * 基于字符串的可转换对象。
 *
 * @author Mara.X.Ma
 * @since 1.0.0
 */
public abstract class AbstractStringBasedConvertible extends AbstractByteArrayBasedConvertible {

    protected final String objectString;

    private static final String DEFAULT_CHARSET = "UTF-8";

    protected AbstractStringBasedConvertible(HttpResponse httpResponse, String charset) {
        super(httpResponse);
        String finalCharset = charset == null ? DEFAULT_CHARSET : charset;
        try {
            if (bytes == null) {
                objectString = null;
            } else {
                objectString = new String(bytes, finalCharset);
            }
        } catch (Exception e) {
            throw new ConversionException("Cannot convert bytes to String with charset '" + finalCharset + "'", e);
        }
    }

    /**
     * 直接返回该对象的字符串形式。
     * @return 字符串
     */
    @Override
    public String toString() {
        return objectString;
    }
}
