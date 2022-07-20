package org.mosin.annohttp.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Converter {
	Charset DEFAULT_STRING_CHARSET = StandardCharsets.UTF_8;
	String DEFAULT_STRING_FORM_FIELD_NAME = "String";
	String DEFAULT_FILE_FORM_FIELD_NAME = "File";
	String CONTENT_TYPE_APPLICATION_YAML = "application/yaml";
	String CONTENT_TYPE_APPLICATION_YML = "application/yml";
	String CONTENT_TYPE_TEXT_YAML = "text/yaml";
	String CONTENT_TYPE_TEXT_YML = "text/yml";
}
