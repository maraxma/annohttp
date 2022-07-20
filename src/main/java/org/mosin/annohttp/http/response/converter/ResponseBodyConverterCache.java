package org.mosin.annohttp.http.response.converter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResponseBodyConverterCache {

    static final Map<Class<? extends ResponseBodyConverter>, ResponseBodyConverter> REG_MAP = new LinkedHashMap<>();

    static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    static final Map<Class<? extends ResponseBodyConverter>, ResponseBodyConverter> DEFAULT_REG_MAP;

    public static final ResponseBodyConverter AUTO_RESPONSE_BODY_CONVERTER = new AutoResponseBodyConverter();

    static {
        LinkedHashMap<Class<? extends ResponseBodyConverter>, ResponseBodyConverter> map = new LinkedHashMap<>();
        map.put(InputStreamResponseBodyConverter.class, new InputStreamResponseBodyConverter());
        map.put(StringResponseBodyConverter.class, new StringResponseBodyConverter());
        map.put(ByteArrayResponseBodyConverter.class, new ByteArrayResponseBodyConverter());
        map.put(HttpResponseResponseBodyConverter.class, new HttpResponseResponseBodyConverter());
        map.put(Json2BeanResponseBodyConverter.class, new Json2BeanResponseBodyConverter());
        map.put(Xml2BeanResponseBodyConverter.class, new Xml2BeanResponseBodyConverter());
        map.put(Yaml2BeanResponseBodyConverter.class, new Yaml2BeanResponseBodyConverter());
        map.put(InputStream2JavaObjectResponseBodyConverter.class, new InputStream2JavaObjectResponseBodyConverter());
        DEFAULT_REG_MAP = Collections.unmodifiableMap(map);
    }

    public static void addUserConverters(ResponseBodyConverter... converters) {
        Lock lock = LOCK.writeLock();
        try {
            lock.lock();
            for (ResponseBodyConverter responseBodyConverter : converters) {
                REG_MAP.put(responseBodyConverter.getClass(), responseBodyConverter);
            }
        } finally {
            lock.unlock();
        }
    }

    public static Map<Class<? extends ResponseBodyConverter>, ResponseBodyConverter> getAll() {
        Lock lock = LOCK.readLock();
        try {
            lock.lock();
            LinkedHashMap<Class<? extends ResponseBodyConverter>, ResponseBodyConverter> map = new LinkedHashMap<>();
            map.putAll(REG_MAP);
            map.putAll(DEFAULT_REG_MAP);
            return Collections.unmodifiableMap(map);
        } finally {
            lock.unlock();
        }
    }
}
