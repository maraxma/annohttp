package org.mosin.annohttp.spring.annotation;

import org.mosin.annohttp.spring.configuration.AnnoHttpBeanFactoryProcessorConfiguration;
import org.mosin.annohttp.spring.configuration.AnnoHttpConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启annohttp的自动装配功能。
 * <p>开启后将借由spring扫描指定目录下的HTTP服务。</p>
 * <p>需要被spring扫描作为HTTP客户端的接口必须标注 {@link org.mosin.annohttp.annotation.AnnoHttpService}。</p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AnnoHttpConfiguration.class, AnnoHttpBeanFactoryProcessorConfiguration.class})
public @interface EnableAnnoHttpAutoAssembling {

}
