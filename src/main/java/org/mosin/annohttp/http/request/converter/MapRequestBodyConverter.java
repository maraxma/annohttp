package org.mosin.annohttp.http.request.converter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.JacksonComponentHolder;
import org.mosin.annohttp.http.exception.ConversionException;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class MapRequestBodyConverter implements RequestBodyConverter {

	@SuppressWarnings("unchecked")
	@Override
	public HttpEntity convert(Object source, ContentType contentType,
			AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
		HttpEntity httpEntity;
		JsonMapper jsonMapper = JacksonComponentHolder.getJsonMapper(false, true, true, false);
		XmlMapper xmlMapper = JacksonComponentHolder.getXmlMapper(false, true, true, false);
		YAMLMapper yamlMapper = JacksonComponentHolder.getYamlMapper(false, true, true, false);
		String tag = "";
		
		try {
			if (contentType == null) {
				tag = "json";
				httpEntity = new StringEntity(jsonMapper.writeValueAsString(source), ContentType.APPLICATION_JSON);
			} else {
				final Charset charset = contentType.getCharset() == null ? DEFAULT_STRING_CHARSET : contentType.getCharset();
				String userMimeType = contentType.getMimeType();
				if (ContentType.APPLICATION_XML.getMimeType().equalsIgnoreCase(userMimeType)
						|| ContentType.TEXT_XML.getMimeType().equalsIgnoreCase(userMimeType)) {
					tag = "xml";
					httpEntity = new StringEntity(xmlMapper.writeValueAsString(source), contentType.withCharset(charset));
				} else if (CONTENT_TYPE_APPLICATION_YAML.equalsIgnoreCase(userMimeType)
						|| CONTENT_TYPE_APPLICATION_YML.equalsIgnoreCase(userMimeType)
						|| CONTENT_TYPE_TEXT_YAML.equalsIgnoreCase(userMimeType)
						|| CONTENT_TYPE_TEXT_YML.equalsIgnoreCase(userMimeType)) {
					tag = "yaml";
					httpEntity = new StringEntity(yamlMapper.writeValueAsString(source), contentType.withCharset(charset));
				} else if (ContentType.APPLICATION_FORM_URLENCODED.getMimeType().equalsIgnoreCase(userMimeType)) {
					tag = "urlencoded";
					httpEntity = new UrlEncodedFormEntity(((Map<String, Object>) source)
							.entrySet()
							.stream()
							.map(e -> new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue())))
							.collect(Collectors.toList()), charset);
				} else if (ContentType.MULTIPART_FORM_DATA.getMimeType().equalsIgnoreCase(userMimeType)) {
					tag = "multipart";
					final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
					((Map<String, Object>) source).forEach((key, obj) -> {
						if (obj instanceof File file) {
							multipartEntityBuilder.addBinaryBody(key, file);
						} else if (obj instanceof byte[] bytes) {
							multipartEntityBuilder.addBinaryBody(key, bytes);
						} else {
							multipartEntityBuilder.addTextBody(key, String.valueOf(obj), ContentType.TEXT_PLAIN.withCharset(charset));
						}
					});
					httpEntity = multipartEntityBuilder.build();
				} else {
					throw new IllegalArgumentException("If @Body represent a Map, its content type can json | xml | yaml | urlencoded | multipart only, or you can not set content type, annohhttp set it as application/json by default");
				}
			}
		} catch (Exception e) {
			throw new ConversionException("Cannot convert map to " + tag, e);
		}

		return httpEntity;
	}

	@Override
	public boolean canConvert(Object source, ContentType contentType,
			AnnoHttpClientMetadata annoHttpClientMetadata, String formFieldName) {
		return source instanceof Map;
	}
	
}
