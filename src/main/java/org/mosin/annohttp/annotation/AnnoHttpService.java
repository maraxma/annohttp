package org.mosin.annohttp.annotation;

import java.lang.annotation.*;

/**
 * 声明一个接口作为annohttp服务接口（HTTP客户端）。
 * <p>使用spring自动装配时必须在接口上附加此注解；当自行使用 {@link org.mosin.annohttp.http.AnnoHttpClients#create(Class)} 创建服务时，接口上可以不标注此注解。
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnoHttpService {
    String baseUrl() default "";
}
