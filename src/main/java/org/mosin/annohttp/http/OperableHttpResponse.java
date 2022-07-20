package org.mosin.annohttp.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.http.exception.ConversionException;

@SuppressWarnings("deprecation")
public class OperableHttpResponse implements HttpResponse, Sequencable {

    protected final HttpResponse httpResponse;

    public OperableHttpResponse(HttpResponse httpResponse) {
        if (httpResponse == null) {
            throw new IllegalArgumentException("httpResponse cannot be null");
        }
        this.httpResponse = httpResponse;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return httpResponse.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(String name) {
        return httpResponse.containsHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return httpResponse.getHeaders(name);
    }

    @Override
    public Header getFirstHeader(String name) {
        return httpResponse.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return httpResponse.getLastHeader(name);
    }

    @Override
    public Header[] getAllHeaders() {
        return httpResponse.getAllHeaders();
    }

    @Override
    public void addHeader(Header header) {
        httpResponse.addHeader(header);
    }

    @Override
    public void addHeader(String name, String value) {
        httpResponse.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        httpResponse.setHeader(header);
    }

    @Override
    public void setHeader(String name, String value) {
        httpResponse.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        httpResponse.setHeaders(headers);
    }

    @Override
    public void removeHeader(Header header) {
        httpResponse.removeHeader(header);
    }

    @Override
    public void removeHeaders(String name) {
        httpResponse.removeHeaders(name);
    }

    @Override
    public HeaderIterator headerIterator() {
        return httpResponse.headerIterator();
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return httpResponse.headerIterator(name);
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
        return httpResponse.getParams();
    }

    @Override
    @Deprecated
    public void setParams(HttpParams params) {
        httpResponse.setParams(params);
    }

    @Override
    public StatusLine getStatusLine() {
        return httpResponse.getStatusLine();
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        httpResponse.setStatusLine(statusline);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        httpResponse.setStatusLine(ver, code);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        httpResponse.setStatusLine(ver, code, reason);
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        httpResponse.setStatusCode(code);
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        httpResponse.setReasonPhrase(reason);
    }

    @Override
    public HttpEntity getEntity() {
        return httpResponse.getEntity();
    }

    @Override
    public void setEntity(HttpEntity entity) {
        httpResponse.setEntity(entity);
    }

    @Override
    public Locale getLocale() {
        return httpResponse.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        httpResponse.setLocale(loc);
    }

    @Override
    public String asSequenceToString() {
        return asSequenceToString(DEFAULT_CHARSET);
    }

    @Override
    public String asSequenceToString(String charset) {
        try {
            byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
            if (bytes == null) {
                return null;
            }
            return new String(bytes, charset);
        } catch (Exception e) {
            throw new ConversionException("Response body cannot convert to String whit charset '" + charset + "'", e);
        }
    }

    @Override
    public byte[] asSequenceToBytes() {
        try {
            return EntityUtils.toByteArray(httpResponse.getEntity());
        } catch (IOException e) {
            throw new ConversionException("Response body cannot convert to byte[]", e);
        }
    }

    @Override
    public void close() throws IOException {
        EntityUtils.consumeQuietly(getEntity());
    }

    @Override
    public InputStream asInputStream() {
        return Optional.of(httpResponse.getEntity()).map(t -> {
            try {
                return t.getContent();
            } catch (UnsupportedOperationException | IOException e) {
                throw new ConversionException("Cannot acquire InputStream from response", e);
            }
        }).orElse(null);
    }

    @Override
    public Object asJavaSerializedSequenceToObject() {
        InputStream inputStream = asInputStream();
        if (inputStream == null) {
            return null;
        }
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ConversionException("Cannot convert response to Java Object", e);
        }
    }

    @Override
    public Convertable asXmlConvertable() {
        return new XmlConvertable(httpResponse, DEFAULT_CHARSET);
    }

    @Override
    public Convertable asJsonConvertable() {
        return new JsonConvertable(httpResponse, DEFAULT_CHARSET);
    }

    @Override
    public Convertable asYamlConvertable() {
        return new YamlConvertable(httpResponse, DEFAULT_CHARSET);
    }

    @Override
    public Convertable asConvertable(BiFunction<HttpResponse, String, Convertable> convertableProducer) {
        if (convertableProducer == null) {
            return null;
        }
        return convertableProducer.apply(httpResponse, DEFAULT_CHARSET);
    }

}
