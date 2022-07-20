package org.mosin.annohttp.http;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * 首字母大写属性命名法
 *
 * @author mm92
 * @since 1.0.6 2018-11-22
 */
public class JacksonCapitalizePropertyNamingStrategy extends PropertyNamingStrategies.NamingBase {

    private static final long serialVersionUID = -4940852506779479299L;

    @Override
    public String translate(String propertyName) {
        return Character.toTitleCase(propertyName.charAt(0)) +
                propertyName.substring(1);
    }
}
