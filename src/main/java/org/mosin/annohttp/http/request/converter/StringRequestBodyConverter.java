package org.mosin.annohttp.http.request.converter;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;

public class StringRequestBodyConverter implements RequestBodyConverter {

    @Override
    public HttpEntity convert(Object source, ContentType contentType,
                              AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
        HttpEntity httpEntity;
        if (contentType == null) {
            httpEntity = new StringEntity((String) source, DEFAULT_STRING_CHARSET);
        } else {
            Charset charset = contentType.getCharset();
            charset = charset == null ? DEFAULT_STRING_CHARSET : charset;
            String fieldName = "".equals(formFieldName) ? DEFAULT_STRING_FORM_FIELD_NAME : formFieldName;
            if (contentType.getMimeType().equalsIgnoreCase(ContentType.TEXT_PLAIN.getMimeType())) {
                httpEntity = new StringEntity((String) source, charset);
            } else if (contentType.getMimeType().equalsIgnoreCase(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                BasicNameValuePair basicNameValuePair = new BasicNameValuePair(fieldName, (String) source);
                httpEntity = new UrlEncodedFormEntity(Collections.singletonList(basicNameValuePair), charset);
            } else if (contentType.getMimeType().equalsIgnoreCase(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                httpEntity = MultipartEntityBuilder.create()
                        .addTextBody(fieldName, (String) source)
                        .setCharset(charset)
                        .build();
            } else if (contentType.getMimeType().equalsIgnoreCase(ContentType.APPLICATION_OCTET_STREAM.getMimeType())) {
                httpEntity = new InputStreamEntity(new ByteArrayInputStream(((String) source).getBytes(charset)));
            } else {
                httpEntity = new StringEntity((String) source, contentType.withCharset(charset));
            }
        }

        return httpEntity;
    }

    @Override
    public boolean canConvert(Object source, ContentType contentType,
                              AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
        return source instanceof String;
    }

}
