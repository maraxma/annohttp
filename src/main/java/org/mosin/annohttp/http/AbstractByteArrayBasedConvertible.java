package org.mosin.annohttp.http;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.http.exception.ConversionException;

import java.util.Arrays;

/**
 * 基于字符串的可转换对象。
 *
 * @author Mara.X.Ma
 * @since 1.0.0
 */
public abstract class AbstractByteArrayBasedConvertible implements Convertible {

    protected final byte[] bytes;

    protected AbstractByteArrayBasedConvertible(HttpResponse httpResponse) {
        
        try {
            bytes = EntityUtils.toByteArray(httpResponse.getEntity());
        } catch (Exception e) {
            throw new ConversionException("Cannot convert response body to bytes", e);
        }
    }

    /**
     * 直接返回该对象的字符串形式。
     * @return 字符串
     */
    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }
}
