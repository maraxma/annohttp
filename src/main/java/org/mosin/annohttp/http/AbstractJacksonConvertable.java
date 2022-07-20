package org.mosin.annohttp.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.http.exception.ConversionException;
import org.mosin.annohttp.http.serialization.TypeRef;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJacksonConvertable extends AbstractConvertable {
	
	protected ObjectMapper objectMapper;
	protected final String objectString; 
	
	private static final String DEFAULT_CHARSET = "UTF-8";

	protected AbstractJacksonConvertable(HttpResponse httpResponse, String charset) {
		super(httpResponse);
		String finalCharset = charset == null ? DEFAULT_CHARSET : charset;
		try {
            // 这个方法会自动关闭流
            byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
            if (bytes == null) {
                objectString = null;
            } else {
            	objectString = new String(bytes, finalCharset);
            }
        } catch (Exception e) {
            throw new ConversionException("Cannot convert response body to String with charset '" + finalCharset + "'", e);
        }
	}

	@Override
	public <T> T toBean(Class<T> clazz) {
		Objects.requireNonNull(clazz);
		try {
			return objectString == null ? null : objectMapper.readValue(objectString, clazz);
		} catch (JsonProcessingException e) {
			throw new ConversionException("Cannot convert response json '" + objectString + "' to class '" + clazz + "'", e);
		}
	}

	@Override
	public <K, V> Map<K, V> toMap(Class<K> keyClass, Class<V> valueClass) {
		return toMap(HashMap.class, keyClass, valueClass);
	}

	@Override
	public <K, V> Map<K, V> toMap(@SuppressWarnings("rawtypes") Class<? extends Map> mapClass, Class<K> keyClass, Class<V> valueClass) {
		Objects.requireNonNull(mapClass);
		Objects.requireNonNull(keyClass);
		Objects.requireNonNull(valueClass);
		Objects.requireNonNull(keyClass);
		Objects.requireNonNull(valueClass);
		try {
			return objectString == null ? null : objectMapper.readValue(objectString, objectMapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass));
		} catch (JsonProcessingException e) {
			throw new ConversionException("Cannot convert response json '" + objectString + "' to Map", e);
		}
	}

	@Override
	public <T> List<T> toList(Class<T> elementClass) {
		return toList(ArrayList.class, elementClass);
	}

	@Override
	public <T> List<T> toList(@SuppressWarnings("rawtypes") Class<? extends List> listClass, Class<T> elementClass) {
		Objects.requireNonNull(listClass);
		Objects.requireNonNull(elementClass);
		try {
			return objectString == null ? null : objectMapper.readValue(objectString, objectMapper.getTypeFactory().constructCollectionType(listClass, elementClass));
		} catch (JsonProcessingException e) {
			throw new ConversionException("Cannot convert response json '" + objectString + "' to List", e);
		}
	}

	@Override
	public <K, V> List<Map<K, V>> toListMap(Class<K> keyClass, Class<V> valueClass) {
		return toListMap(List.class, Map.class, keyClass, valueClass);
	}

	@Override
	public <K, V> List<Map<K, V>> toListMap(@SuppressWarnings("rawtypes") Class<? extends List> listClass,
			@SuppressWarnings("rawtypes") Class<? extends Map> mapClass, Class<K> keyClass, Class<V> valueClass) {
		Objects.requireNonNull(listClass);
		Objects.requireNonNull(mapClass);
		Objects.requireNonNull(keyClass);
		Objects.requireNonNull(valueClass);
		try {
			return objectString == null ? null : objectMapper.readValue(objectString, objectMapper.getTypeFactory()
					.constructCollectionType(listClass, objectMapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass)));
		} catch (JsonProcessingException e) {
			throw new ConversionException("Cannot convert response json '" + objectString + "' to ListMap", e);
		}
	}

	@Override
	public <T> T toCollection(@SuppressWarnings("rawtypes") Class<? extends Collection> collectionClass, Class<?> elementClass) {
		Objects.requireNonNull(collectionClass);
		Objects.requireNonNull(elementClass);
		try {
			return objectString == null ? null : objectMapper.readValue(objectString, objectMapper.getTypeFactory()
					.constructCollectionType(collectionClass, elementClass));
		} catch (JsonProcessingException e) {
			throw new ConversionException("Cannot convert response json '" + objectString + "' to Collection", e);
		}
	}

	@Override
	public <T> T toSpecified(TypeRef<T> typeRef) {
		Objects.requireNonNull(typeRef);
		JavaType javaType = objectMapper.constructType(typeRef.getType());
		try {
			return objectString == null ? null : objectMapper.readValue(objectString, javaType);
		} catch (JsonProcessingException e) {
			throw new ConversionException("Cannot convert response json '" + objectString + "' to '" + typeRef.getType() + "'", e);
		}
	}

	@Override
	public void close() throws IOException {
		EntityUtils.consumeQuietly(httpResponse.getEntity());
	}

}
