package org.mosin.annohttp.http;

import java.io.Closeable;
import java.io.InputStream;
import java.util.function.BiFunction;

import org.apache.http.HttpResponse;

/**
 * 代表一个可以视为序列的对象。
 */
public interface Sequencable extends Closeable {

    String DEFAULT_CHARSET = "UTF-8";

    InputStream asInputStream();

    String asSequenceToString();

    String asSequenceToString(String charset);

    byte[] asSequenceToBytes();

    Object asJavaSerializedSequenceToObject();

    Convertible asXmlConvertible();

    Convertible asJsonConvertible();

    Convertible asYamlConvertible();

    Convertible asConvertible(BiFunction<HttpResponse, String, Convertible> convertibleProducer);

}
