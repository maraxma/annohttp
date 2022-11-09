package org.mosin.annohttp.http;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mosin.annohttp.http.serialization.TypeRef;

/**
 * 代表一个可转换的对象。特别地，annohttp将响应体作为一个可转换对象。
 */
public interface Convertible {

    <T> T toBean(Class<T> clazz);

    <K, V> Map<K, V> toMap(Class<K> keyClass, Class<V> valueClass);

    <K, V> Map<K, V> toMap(@SuppressWarnings("rawtypes") Class<? extends Map> mapClass, Class<K> keyClass, Class<V> valueClass);

    <T> List<T> toList(Class<T> elementClass);

    <T> List<T> toList(@SuppressWarnings("rawtypes") Class<? extends List> listClass, Class<T> elementClass);

    <K, V> List<Map<K, V>> toListMap(Class<K> keyClass, Class<V> valueClass);

    <K, V> List<Map<K, V>> toListMap(@SuppressWarnings("rawtypes") Class<? extends List> listClass, @SuppressWarnings("rawtypes") Class<? extends Map> mapClass, Class<K> keyClass, Class<V> valueClass);

    <T> T toCollection(@SuppressWarnings("rawtypes") Class<? extends Collection> collectionClass, Class<?> elementClass);

    <T> T toSpecified(TypeRef<T> typeRef);
}
