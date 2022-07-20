package org.mosin.annohttp.http.response.converter;

import java.util.Set;

import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.JacksonComponentHolder;

public class Yaml2BeanResponseBodyConverter extends AbstractJackson2BeanResponseBodyConverter {

	public Yaml2BeanResponseBodyConverter() {
		super();
		objectMapper = JacksonComponentHolder.getJsonMapper(false, true, true, false);
		acceptableContentTypes = Set.of(ContentType.create("application/yml"), ContentType.create("application/yaml"),
				ContentType.create("text/yml"), ContentType.create("text/yaml"));
		name = "yaml";
	}
}
