package org.mosin.annohttp.http.response.converter;

import java.util.Set;

import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.JacksonComponentHolder;

public class Json2BeanResponseBodyConverter extends AbstractJackson2BeanResponseBodyConverter {

    public Json2BeanResponseBodyConverter() {
        super();
        objectMapper = JacksonComponentHolder.getJsonMapper(false, true, true, false);
        acceptableContentTypes = Set.of(ContentType.APPLICATION_JSON, ContentType.create("text/json"));
        name = "json";
    }

}
