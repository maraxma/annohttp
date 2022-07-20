package org.mosin.annohttp.http.response.converter;

import java.nio.charset.Charset;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.mosin.annohttp.http.AnnoHttpClientMetadata;
import org.mosin.annohttp.http.exception.ConversionException;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJackson2BeanResponseBodyConverter implements ResponseBodyConverter {
	
	protected ObjectMapper objectMapper;
	protected Set<ContentType> acceptableContentTypes;
	protected String name;

	@Override
	public boolean canConvert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata,
			ContentType computedResponseContentType, Charset computedResponseCharset) {
		return httpResponse.getEntity() != null 
				&& httpResponse.getEntity().isStreaming() 
				&& acceptableContentTypes.stream().anyMatch(e -> e.getMimeType().equalsIgnoreCase(computedResponseContentType.getMimeType()));
	}

	@Override
	public Object convert(HttpResponse httpResponse, AnnoHttpClientMetadata metadata,
			ContentType computedResponseContentType, Charset computedResponseCharset) {
		try {
			String jsonString = EntityUtils.toString(httpResponse.getEntity(), computedResponseCharset);
			return objectMapper.readValue(jsonString, objectMapper.constructType(metadata.getRequestMethodActualType()));
		} catch (Exception e) {
			throw new ConversionException(this, "Cannot convert response body to " + name, e);
		}
	}

}
