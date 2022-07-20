package org.mosin.annohttp.http.response.converter;

import java.util.Set;

import org.apache.http.entity.ContentType;
import org.mosin.annohttp.http.JacksonComponentHolder;

public class Xml2BeanResponseBodyConverter extends AbstractJackson2BeanResponseBodyConverter {

	public Xml2BeanResponseBodyConverter() {
		super();
		objectMapper = JacksonComponentHolder.getJsonMapper(false, true, true, false);
		acceptableContentTypes = Set.of(ContentType.APPLICATION_XML, ContentType.TEXT_XML);
		name = "xml";
	}
}
